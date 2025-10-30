package com.celada.notification.system.service;

import com.celada.notification.system.models.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class EmailService implements NotificationService {
    public Mono<Boolean> sendNotification(NotificationEvent event) {
        return Mono.fromCallable(() -> {
            Thread.sleep(300);
            // Simulate error with 15% probability
            if (ThreadLocalRandom.current().nextInt(100) < 15) {
                throw new RuntimeException("Failed to send notification to Email");
            }
            log.info("Notification sent to Email: {}", event);
            return true;
        });
    }
}
