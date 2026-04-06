package com.bookinghub.search.application.dto;

import java.util.List;

public record EstablishmentFilterInput(
        String query,
        String city,
        String state,
        GeoFilterInput geo,
        List<String> services,
        Double minRating,
        Double minPrice,
        Double maxPrice,
        String sortBy
) {}
