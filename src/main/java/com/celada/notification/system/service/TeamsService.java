package com.celada.notification.system.service;

import com.celada.notification.system.models.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class TeamsService implements NotificationService {
    public Mono<Boolean> sendNotification(NotificationEvent event) {
        return Mono.fromCallable(() -> {
            Thread.sleep(150);
            // Simulate error with 10% probability
            if (ThreadLocalRandom.current().nextInt(10) == 0) {
                throw new RuntimeException("Failed to send notification to Teams");
            }
            log.info("Notification sent to Teams: {}", event);
            return true;
        });
    }
}
