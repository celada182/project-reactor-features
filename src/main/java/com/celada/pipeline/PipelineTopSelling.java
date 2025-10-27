package com.celada.pipeline;

import com.celada.database.Database;
import com.celada.models.Videogame;
import reactor.core.publisher.Flux;

public class PipelineTopSelling {
    /**
     * @return Videogames sold more than 80 copies
     */
    public static Flux<String> topSelling() {
        return Database.getDataAsFlux()
                .filter(videogame -> videogame.getTotalSold() > 80)
                .map(Videogame::getName);
    }
}
