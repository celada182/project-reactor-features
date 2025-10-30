package com.celada.notification.system;

import com.celada.notification.system.models.NotificationEvent;
import com.celada.notification.system.models.NotificationStatus;
import com.celada.notification.system.models.Priority;
import com.celada.notification.system.service.EmailService;
import com.celada.notification.system.service.NotificationService;
import com.celada.notification.system.service.PhoneService;
import com.celada.notification.system.service.TeamsService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class NotificationSystem {

    private static final String TEAMS_CHANNEL = "Teams-Channel";
    private static final String EMAIL_CHANNEL = "Email-Channel";
    private static final String PHONE_CHANNEL = "Phone-Channel";

    private final Sinks.Many<NotificationEvent> mainEventSink;
    @Getter
    private final Sinks.Many<NotificationEvent> historySink;


    private final NotificationService teamsService;
    private final NotificationService emailService;
    private final NotificationService phoneService;

    private final Sinks.One<NotificationEvent> teamsSink;
    private final Sinks.One<NotificationEvent> emailSink;
    private final Sinks.One<NotificationEvent> phoneSink;

    // Simulate cache
    private final ConcurrentMap<String, NotificationEvent> notificationCache;

    public NotificationSystem() {
        // Multiple channel cast
        // Buffer when subscriber is busy
        this.mainEventSink = Sinks.many().multicast().onBackpressureBuffer();
        // Store 50 events on history
        this.historySink = Sinks.many().replay().limit(50);

        this.teamsService = new TeamsService();
        this.emailService = new EmailService();
        this.phoneService = new PhoneService();

        this.teamsSink = Sinks.one();
        this.emailSink = Sinks.one();
        this.phoneSink = Sinks.one();

        this.notificationCache = new ConcurrentHashMap<>();

        this.setupProcessingFlows();
    }

    NotificationSystem(NotificationService teamsService, NotificationService emailService, NotificationService phoneService) {
        // Multiple channel cast
        // Buffer when subscriber is busy
        this.mainEventSink = Sinks.many().multicast().onBackpressureBuffer();
        // Store 50 events on history
        this.historySink = Sinks.many().replay().limit(50);

        this.teamsService = teamsService;
        this.emailService = emailService;
        this.phoneService = phoneService;

        this.teamsSink = Sinks.one();
        this.emailSink = Sinks.one();
        this.phoneSink = Sinks.one();

        this.notificationCache = new ConcurrentHashMap<>();

        this.setupProcessingFlows();
    }

    public void publishEvent(NotificationEvent event) {
        this.mainEventSink.tryEmitNext(event);
    }

    public Flux<NotificationEvent> getNotificationsHistory() {
        return this.historySink.asFlux();
    }

    public Mono<NotificationEvent> getNotificationById(String id) {
        return Mono.justOrEmpty(this.notificationCache.get(id));
    }

    public Flux<NotificationEvent> retryFailedNotification() {
        return Flux.fromIterable(this.notificationCache.values())
                .filter(event -> NotificationStatus.FAILED.equals(event.getStatus()))
                .doOnNext(this::publishEvent);
    }

    private void setupProcessingFlows() {
        this.mainEventSink.asFlux()
                .doOnNext(event -> log.info("Processing event: {}", event))
                .doOnNext(this::updateEventStatus)
                .doOnNext(this.historySink::tryEmitNext)
                .subscribe(this::routeEventByPriority);

        this.setupTeamsProcessor();
        this.setupEmailProcessor();
        this.setupPhoneProcessor();
    }

    private void setupTeamsProcessor() {
        this.teamsSink
                .asMono()
                .flatMap(event ->
                        this.teamsService.sendNotification(event)
                                .subscribeOn(Schedulers.boundedElastic())
                                .doOnSuccess(success -> this.updateSuccessStatus(event, TEAMS_CHANNEL))
                                .doOnError(error -> this.updateErrorStatus(event, TEAMS_CHANNEL, error))
                                .onErrorResume(error -> Mono.just(false))
                )
                .subscribe();
    }

    private void setupEmailProcessor() {
        this.emailSink
                .asMono()
                .flatMap(event ->
                        this.emailService.sendNotification(event)
                                .subscribeOn(Schedulers.boundedElastic())
                                .doOnSuccess(success -> this.updateSuccessStatus(event, EMAIL_CHANNEL))
                                .doOnError(error -> this.updateErrorStatus(event, EMAIL_CHANNEL, error))
                                .onErrorResume(error -> Mono.just(false))
                )
                .subscribe();
    }

    private void setupPhoneProcessor() {
        this.phoneSink
                .asMono()
                .flatMap(event ->
                        this.phoneService.sendNotification(event)
                                .subscribeOn(Schedulers.boundedElastic())
                                .retry(3)
                                .doOnSuccess(success -> this.updateSuccessStatus(event, PHONE_CHANNEL))
                                .doOnError(error -> this.updateErrorStatus(event, PHONE_CHANNEL, error))
                                .onErrorResume(error -> Mono.just(false))
                )
                .subscribe();
    }

    private void updateEventStatus(NotificationEvent event) {
        if (Objects.isNull(event.getStatus())) {
            event.setId(UUID.randomUUID().toString());
            event.setStatus(NotificationStatus.PENDING);
        }

        this.notificationCache.put(event.getId(), event);
    }

    private void updateErrorStatus(NotificationEvent event, String channel, Throwable error) {
        log.error("Error sending notification {} to channel {}: {}", event, channel, error);
        NotificationEvent cachedEvent = this.notificationCache.get(event.getId());
        if (Objects.nonNull(cachedEvent)) {
            cachedEvent.setStatus(NotificationStatus.FAILED);
            this.historySink.tryEmitNext(cachedEvent);
        }
    }

    private void updateSuccessStatus(NotificationEvent event, String channel) {
        log.info("Notification {} sent to channel {}", event, channel);
        NotificationEvent cachedEvent = this.notificationCache.get(event.getId());
        if (Objects.nonNull(cachedEvent)) {
            cachedEvent.setStatus(NotificationStatus.SENT);
            this.historySink.tryEmitNext(cachedEvent);
        }
    }

    private void routeEventByPriority(NotificationEvent event) {
        this.teamsSink.tryEmitValue(event);

        if (Priority.HIGH.equals(event.getPriority()) || Priority.MEDIUM.equals(event.getPriority())) {
            this.emailSink.tryEmitValue(event);
        }

        if (Priority.HIGH.equals(event.getPriority())) {
            this.phoneSink.tryEmitValue(event);
        }
    }
}
