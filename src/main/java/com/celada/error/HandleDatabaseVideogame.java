package com.celada.error;

import com.celada.database.Database;
import com.celada.models.Console;
import com.celada.models.Videogame;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class HandleDatabaseVideogame {
    public static Flux<Videogame> handleDatabaseVideogames() {
        return Database.getDataAsFlux()
                .handle((vg, sink) -> {
                    if (Console.DISABLED == vg.getConsole()) {
                        sink.error(new RuntimeException("Videogame disabled"));
                        return;
                    }
                    sink.next(vg);
                })
                .onErrorResume(error -> {
                    // Replace flux
                    log.error("Error: {}", error.getMessage());
                    return Flux.merge(
                            Database.getDataAsFlux(),
                            Database.fluxAssassinsDefault
                    );
                })
                .cast(Videogame.class)
                .distinct(Videogame::getName);
    }

    public static Flux<Videogame> handleDatabaseVideogamesDefault() {
        return Database.getDataAsFlux()
                .handle((vg, sink) -> {
                    if (Console.DISABLED == vg.getConsole()) {
                        sink.error(new RuntimeException("Videogame disabled"));
                        return;
                    }
                    sink.next(vg);
                })
                // Default response
                .onErrorReturn(Database.DEFAULT_VIDEOGAME)
                .cast(Videogame.class);
    }
}
