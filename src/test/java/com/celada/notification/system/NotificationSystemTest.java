package com.celada.notification.system;

import com.celada.notification.system.models.NotificationEvent;
import com.celada.notification.system.models.NotificationStatus;
import com.celada.notification.system.models.Priority;
import com.celada.notification.system.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationSystemTest {

    private NotificationService mockTeamsService;
    private NotificationService mockEmailService;
    private NotificationService mockPhoneService;

    private NotificationSystem target;

    private AtomicInteger teamsCallCount;
    private AtomicInteger emailCallCount;
    private AtomicInteger phoneCallCount;

    @BeforeEach
    public void setUp() {
        this.mockTeamsService = mock(NotificationService.class);
        this.mockEmailService = mock(NotificationService.class);
        this.mockPhoneService = mock(NotificationService.class);

        this.teamsCallCount = new AtomicInteger(0);
        this.emailCallCount = new AtomicInteger(0);
        this.phoneCallCount = new AtomicInteger(0);

        when(this.mockTeamsService.sendNotification(any(NotificationEvent.class)))
                .thenAnswer(i -> {
                    this.teamsCallCount.incrementAndGet();
                    return Mono.just(true);
                });
        when(this.mockEmailService.sendNotification(any(NotificationEvent.class)))
                .thenAnswer(i -> {
                    this.emailCallCount.incrementAndGet();
                    return Mono.just(true);
                });
        when(this.mockPhoneService.sendNotification(any(NotificationEvent.class)))
                .thenAnswer(i -> {
                    this.phoneCallCount.incrementAndGet();
                    return Mono.just(true);
                });

        this.target = new NotificationSystem(
                this.mockTeamsService,
                this.mockEmailService,
                this.mockPhoneService
        );
    }

    @Test
    @DisplayName("Should send event with LOW priority")
    void testLowPriority() {
        NotificationEvent event = this.createTestEvent(Priority.LOW);
        this.target.publishEvent(event);
        // Sleep test thread to allow reactive streams to finish
        this.sleep(1000);

        verify(mockTeamsService, times(1)).sendNotification(any());
        assertEquals(1, this.teamsCallCount.get());

        verify(mockEmailService, never()).sendNotification(any());
        assertEquals(0, this.emailCallCount.get());

        verify(mockPhoneService, never()).sendNotification(any());
        assertEquals(0, this.phoneCallCount.get());

    }

    @Test
    @DisplayName("Should send event with MEDIUM priority")
    void testMediumPriority() {
        NotificationEvent event = this.createTestEvent(Priority.MEDIUM);
        this.target.publishEvent(event);
        // Sleep test thread to allow reactive streams to finish
        this.sleep(1000);

        verify(mockTeamsService, times(1)).sendNotification(any());
        assertEquals(1, this.teamsCallCount.get());

        verify(mockEmailService, times(1)).sendNotification(any());
        assertEquals(1, this.emailCallCount.get());

        verify(mockPhoneService, never()).sendNotification(any());
        assertEquals(0, this.phoneCallCount.get());

    }

    @Test
    @DisplayName("Should send event with HIGH priority")
    void testHighPriority() {
        NotificationEvent event = this.createTestEvent(Priority.HIGH);
        this.target.publishEvent(event);
        // Sleep test thread to allow reactive streams to finish
        this.sleep(1000);

        verify(mockTeamsService, times(1)).sendNotification(any());
        assertEquals(1, this.teamsCallCount.get());

        verify(mockEmailService, times(1)).sendNotification(any());
        assertEquals(1, this.emailCallCount.get());

        verify(mockPhoneService, times(1)).sendNotification(any());
        assertEquals(1, this.phoneCallCount.get());

    }

    @Test
    @DisplayName("Should keep history of 3 events")
    void shouldHistoryKeep3Events() {
        NotificationEvent event1 = this.createTestEvent(Priority.LOW);
        NotificationEvent event2 = this.createTestEvent(Priority.MEDIUM);
        NotificationEvent event3 = this.createTestEvent(Priority.HIGH);

        this.target.publishEvent(event1);
        this.target.publishEvent(event2);
        this.target.publishEvent(event3);

        this.sleep(1000);

        StepVerifier.create(this.target.getNotificationsHistory().take(3))
                .expectNextCount(3)
                .verifyComplete();
    }


//    @Test
//    @DisplayName("Should retry phone notification 3 times")
//    void shouldRetryPhoneNotification3Times() {
//        AtomicInteger attempts = new AtomicInteger(0);
//        when(mockPhoneService.sendNotification(any(NotificationEvent.class)))
//                .thenAnswer(i -> {
//                   int currentAttempt = attempts.incrementAndGet();
//                   if (currentAttempt <= 2) {
//                       return Mono.error(new RuntimeException("Failed to send phone notification"));
//                   } else {
//                       this.phoneCallCount.incrementAndGet();
//                       return Mono.just(true);
//                   }
//                });
//
//        NotificationEvent event = this.createTestEvent(Priority.HIGH);
//        this.target.publishEvent(event);
//        this.sleep(2000);
//
//        verify(mockPhoneService, times(1)).sendNotification(any());
//        assertEquals(3, this.phoneCallCount.get());
//    }

    @Test
    @DisplayName("Test virtual time")
    void testVirtualTime() {

        VirtualTimeScheduler scheduler = VirtualTimeScheduler.create();

        NotificationService teams = mock(NotificationService.class);
        NotificationService email = mock(NotificationService.class);
        NotificationService phone = mock(NotificationService.class);

        when(teams.sendNotification(any(NotificationEvent.class)))
                .thenAnswer(i ->
                        Mono.just(true).delayElement(Duration.ofMillis(150), scheduler));

        when(email.sendNotification(any(NotificationEvent.class)))
                .thenAnswer(i ->
                        Mono.just(true).delayElement(Duration.ofMillis(300), scheduler));

        when(phone.sendNotification(any(NotificationEvent.class)))
                .thenAnswer(i ->
                        Mono.just(true).delayElement(Duration.ofMillis(1000), scheduler));

        NotificationSystem target = new NotificationSystem(
                teams,
                email,
                phone
        );

        NotificationEvent event = this.createTestEvent(Priority.HIGH);
        target.publishEvent(event);

        scheduler.advanceTimeBy(Duration.ofMillis(1500));
        StepVerifier.withVirtualTime(() -> target.getNotificationsHistory().take(1))
                .expectNextMatches(element -> NotificationStatus.SENT.equals(element.getStatus()))
                .verifyComplete();
    }

    private NotificationEvent createTestEvent(Priority priority) {
        return NotificationEvent.builder()
                .id(UUID.randomUUID().toString())
                .source("TEST")
                .message("Test msg with priority: " + priority.toString())
                .priority(priority)
                .timestamp(LocalDateTime.now())
                .status(NotificationStatus.PENDING)
                .build();
    }

    // Process ending before threads finish
    private void sleep(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}