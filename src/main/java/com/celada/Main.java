package com.celada;

import com.celada.pipeline.PipelineAllComments;
import com.celada.pipeline.PipelineSumAllPricesInDiscount;
import com.celada.pipeline.PipelineTopSelling;
import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Log
public class Main {
    public static void main(String[] args) {

        // Publisher
        Mono<String> mono = Mono.just("Hello").doOnNext(v -> log.info("[onNext]: " + v)).doOnSuccess(v -> log.info("[onSuccess]: " + v)).doOnError(error -> log.info("[onError]: " + error.getMessage()));

        // Consumer
        mono.subscribe(data -> log.info("Receiving Mono Data: " + data), error -> log.info("Mono Error: " + error.getMessage()), () -> log.info("Mono Success"));

        // Publisher
        Flux<String> flux = Flux.just("Java", "Spring", "Reactor").doOnNext(v -> log.info("[onNext]: " + v)).doOnComplete(() -> log.info("[onComplete]"));

        // Consumer
        flux.subscribe(data -> log.info("Receiving Flux Data: " + data), error -> log.info("Flux Error: " + error.getMessage()), () -> log.info("Flux Success"));

        PipelineTopSelling.topSelling().subscribe(log::info);

        PipelineSumAllPricesInDiscount.sumAllPricesInDiscount().subscribe(v -> log.info("Sum of prices in discount: " + v));

        PipelineAllComments.getAllReviewsComments().subscribe(log::info);

        Flux<String> fluxA = Flux.just("1", "2", "3");
        Flux<String> fluxB = Flux.just("A", "B", "C");

        Flux<String> combinedFlux = fluxA.flatMap(sA -> fluxB.map(sB -> sA + " - " + sB));

        combinedFlux.map(String::toLowerCase).doOnNext(log::info).subscribe();

        // Call MS shipments
        Flux<String> shipments = Flux.just("Shipment 1", "Shipment 2", "Shipment 3").delayElements(Duration.ofMillis(120));
        Flux<String> warehouse = Flux.just("Warehouse 1", "Warehouse 2", "Warehouse 3").delayElements(Duration.ofMillis(50));
        Flux<String> payments = Flux.just("Payment 1", "Payment 2", "Payment 3").delayElements(Duration.ofMillis(150));
        Flux<String> confirmations = Flux.just("Confirmation 1", "Confirmation 2", "Confirmation 3").delayElements(Duration.ofMillis(20));

        Flux<String> reports = Flux.zip(shipments, warehouse, (s, w) -> "Shipment: " + s + "\nWarehouse: " + w);

        reports.doOnNext(log::info).blockLast();

        Flux<String> reports2 = Flux.zip(shipments, warehouse, payments, confirmations)
                .map(tuple ->
                        "Shipment: " + tuple.getT1() + "\nWarehouse: " + tuple.getT2() + "\nPayment: " + tuple.getT3() + "\nConfirmation: " + tuple.getT4());

        reports2.doOnNext(log::info).blockLast();
    }
}