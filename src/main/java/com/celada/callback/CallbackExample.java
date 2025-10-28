package com.celada.callback;

import com.celada.database.Database;
import com.celada.models.Videogame;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
public class CallbackExample {
    public static Flux<Videogame> callbacks() {
        return Database.getDataAsFlux()
                //.delayElements(Duration.ofMillis(500))
                // Force error
                //.timeout(Duration.ofMillis(300))
                .doOnSubscribe(s -> log.info("Subscribed to database"))
                .doOnRequest(n -> log.info("Requesting {} elements", n))
                .doOnNext(v -> log.info("Next element {}", v))
                .doOnCancel(() -> log.warn("Cancelled subscription"))
                .doOnError(error -> log.error("Error: {}", error.getMessage()))
                .doOnComplete(() -> log.info("Completed subscription"))
                .doOnTerminate(() -> log.info("Terminated subscription"))
                .doFinally(signalType -> log.info("Finally: Signal type: {}", signalType));
    }
}
