package com.celada;

import com.celada.callback.CallbackExample;
import com.celada.error.FallbackService;
import com.celada.error.HandleDatabaseVideogame;
import com.celada.pipeline.PipelineAllComments;
import com.celada.pipeline.PipelineSumAllPricesInDiscount;
import com.celada.pipeline.PipelineTopSelling;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
public class Main {
    public static void main(String[] args) {

        // Publisher
        Mono<String> mono = Mono.just("Hello").doOnNext(v -> log.info("[onNext]: {}", v)).doOnSuccess(v -> log.info("[onSuccess]: {}", v)).doOnError(error -> log.info("[onError]: " + error.getMessage()));

        // Consumer
        mono.subscribe(data -> log.info("Receiving Mono Data: " + data), error -> log.info("Mono Error: {}", error.getMessage()), () -> log.info("Mono Success"));

        // Publisher
        Flux<String> flux = Flux.just("Java", "Spring", "Reactor").doOnNext(v -> log.info("[onNext]: {}", v)).doOnComplete(() -> log.info("[onComplete]"));

        // Consumer
        flux.subscribe(data -> log.info("Receiving Flux Data: " + data), error -> log.info("Flux Error: {}", error.getMessage()), () -> log.info("Flux Success"));

        PipelineTopSelling.topSelling().subscribe(log::info);

        PipelineSumAllPricesInDiscount.sumAllPricesInDiscount().subscribe(v -> log.info("Sum of prices in discount: {}", v));

        PipelineAllComments.getAllReviewsComments().subscribe(log::info);

        Flux<String> fluxA = Flux.just("1", "2", "3");
        Flux<String> fluxB = Flux.just("A", "B", "C");

        Flux<String> combinedFlux = fluxA.flatMap(sA -> fluxB.map(sB -> sA + " - " + sB));

        combinedFlux.map(String::toLowerCase).doOnNext(log::info).subscribe();

        log.info("------- Zip Example -------");

        // Call MS shipments
        Flux<String> shipments = Flux.just("Shipment 1", "Shipment 2", "Shipment 3", "Shipment 4").delayElements(Duration.ofMillis(120));
        Flux<String> warehouse = Flux.just("Warehouse 1", "Warehouse 2", "Warehouse 3").delayElements(Duration.ofMillis(50));
        Flux<String> payments = Flux.just("Payment 1", "Payment 2", "Payment 3").delayElements(Duration.ofMillis(150));
        Flux<String> confirmations = Flux.just("Confirmation 1", "Confirmation 2", "Confirmation 3").delayElements(Duration.ofMillis(20));

        Flux<String> reports = Flux.zip(shipments, warehouse, (s, w) -> "Shipment: " + s + " Warehouse: " + w);

        reports.doOnNext(log::info).blockLast();

        Flux<String> reports2 = Flux.zip(shipments, warehouse, payments, confirmations)
                .map(tuple ->
                        "Shipment: " + tuple.getT1() + " Warehouse: " + tuple.getT2() + " Payment: " + tuple.getT3() + " Confirmation: " + tuple.getT4());

        reports2.doOnNext(log::info).blockLast();

        log.info("------- Handle Example -------");

        HandleDatabaseVideogame.handleDatabaseVideogames()
                .subscribe(v -> log.info(v.toString()));

        log.info("------- Handle Default Example -------");

        HandleDatabaseVideogame.handleDatabaseVideogamesDefault()
                .subscribe(v -> log.info(v.toString()));

        log.info("------- Fallback Example -------");

        FallbackService.callFallback()
                .subscribe(v -> log.info(v.toString()));

        log.info("------- Callback Example -------");

        CallbackExample.callbacks()
                .subscribe(data -> log.debug(data.getName()),
                        error -> log.error(error.getMessage()),
                        () -> log.debug("Finally"));
    }
}