package com.celada.notification.system.service;

import com.celada.notification.system.models.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class PhoneService implements NotificationService {
    public Mono<Boolean> sendNotification(NotificationEvent event) {
        return Mono.fromCallable(() -> {
            Thread.sleep(1000);
            // Simulate error with 20% probability
            if (ThreadLocalRandom.current().nextInt(100) < 20) {
                throw new RuntimeException("Failed to send notification to Phone");
            }
            log.info("Notification sent to Phone: {}", event);
            return true;
        });
    }
}
