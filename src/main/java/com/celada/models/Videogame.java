package com.celada.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Videogame {
    private String name;
    private Double price;
    private Console console;
    private List<Review> reviews;
    private String officialWebsite;
    private boolean isDiscount;
    private Integer totalSold;
}
