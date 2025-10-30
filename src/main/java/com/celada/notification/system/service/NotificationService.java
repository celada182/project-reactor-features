package com.celada.notification.system.service;

import com.celada.notification.system.models.NotificationEvent;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Boolean> sendNotification(NotificationEvent event);
}
