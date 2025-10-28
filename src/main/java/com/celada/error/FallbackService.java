package com.celada.error;

import com.celada.database.Database;
import com.celada.models.Console;
import com.celada.models.Videogame;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class FallbackService {
    public static Flux<Videogame> callFallback() {
        return Database.getDataAsFlux()
                .handle((vg, sink) -> {
                    if (Console.DISABLED == vg.getConsole()) {
                        sink.error(new RuntimeException("Videogame disabled"));
                        return;
                    }
                    sink.next(vg);
                })
                // Retry flux
                .retry(5)
                .onErrorResume(error -> {
                    log.error("Database Error: {}", error.getMessage());
                    return Database.fluxFallback;
                })
                // Repeat all flux
                .repeat(1)
                .cast(Videogame.class);
    }
}
