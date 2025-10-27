package com.celada.pipeline;

import com.celada.database.Database;
import com.celada.models.Videogame;
import reactor.core.publisher.Mono;

public class PipelineSumAllPricesInDiscount {
    public static Mono<Double> sumAllPricesInDiscount() {
        return Database.getDataAsFlux()
                .filter(Videogame::isDiscount)
                .map(Videogame::getPrice)
                .reduce(0.0, Double::sum);
    }
}
