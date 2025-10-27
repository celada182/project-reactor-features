package com.celada.pipeline;

import com.celada.database.Database;
import com.celada.models.Review;
import reactor.core.publisher.Flux;

public class PipelineAllComments {
    public static Flux<String> getAllReviewsComments(){
        return Database.getDataAsFlux()
                .flatMap(videogame -> Flux.fromIterable(videogame.getReviews()))
                .map(Review::getComment);
    }
}
