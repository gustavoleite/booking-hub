package com.bookinghub.search.core.domain;

import lombok.*;
import java.util.List;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class SearchFilter {
    private String query;
    private String city;
    private String state;
    private Double geoLat;
    private Double geoLon;
    private Double geoRadiusKm;
    private List<String> services;
    private Double minRating;
    private Double minPrice;
    private Double maxPrice;
    private SortBy sortBy;

    public boolean hasGeo() {
        return geoLat != null && geoLon != null && geoRadiusKm != null;
    }

    public enum SortBy {
        RELEVANCE, RATING, DISTANCE
    }
}
