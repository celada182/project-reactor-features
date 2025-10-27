package com.celada;

import com.celada.pipeline.PipelineSumAllPricesInDiscount;
import com.celada.pipeline.PipelineTopSelling;
import lombok.extern.java.Log;

@Log
public class Main {
    public static void main(String[] args) {

//        // Publisher
//        Mono<String> mono = Mono.just("Hello")
//                .doOnNext(v -> log.info("[onNext]: " + v))
//                .doOnSuccess(v -> log.info("[onSuccess]: " + v))
//                .doOnError(error -> log.info("[onError]: " + error.getMessage()));
//
//        // Consumer
//        mono.subscribe(
//                data -> log.info("Receiving Mono Data: " + data),
//                error -> log.info("Mono Error: " + error.getMessage()),
//                () -> log.info("Mono Success")
//        );
//
//        // Publisher
//        Flux<String> flux = Flux.just("Java", "Spring", "Reactor")
//                .doOnNext(v -> log.info("[onNext]: " + v))
//                .doOnComplete(() -> log.info("[onComplete]"));
//
//        // Consumer
//        flux.subscribe(
//                data -> log.info("Receiving Flux Data: " + data),
//                error -> log.info("Flux Error: " + error.getMessage()),
//                () -> log.info("Flux Success")
//        );

        PipelineTopSelling.topSelling()
                .subscribe(log::info);

        PipelineSumAllPricesInDiscount.sumAllPricesInDiscount()
                .subscribe(v -> log.info("Sum of prices in discount: " + v));
    }
}