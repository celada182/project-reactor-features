package com.celada;

import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log
public class Main {
    public static void main(String[] args) {

        // Publisher
        Mono<String> mono = Mono.just("Hello")
                .doOnNext(v -> log.info("[onNext]: " + v))
                .doOnSuccess(v -> log.info("[onSuccess]: " + v))
                .doOnError(error -> log.info("[onError]: " + error.getMessage()));

        // Consumer
        mono.subscribe(
                data -> log.info("Receiving Mono Data: " + data),
                error -> log.info("Mono Error: " + error.getMessage()),
                () -> log.info("Mono Success")
        );

        // Publisher
        Flux<String> flux = Flux.just("Java", "Spring", "Reactor")
                .doOnNext(v -> log.info("[onNext]: " + v))
                .doOnComplete(() -> log.info("[onComplete]"));

        // Consumer
        flux.subscribe(
                data -> log.info("Receiving Flux Data: " + data),
                error -> log.info("Flux Error: " + error.getMessage()),
                () -> log.info("Flux Success")
        );
    }
}