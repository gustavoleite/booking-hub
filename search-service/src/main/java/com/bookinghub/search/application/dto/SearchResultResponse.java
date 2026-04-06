package com.bookinghub.search.application.dto;

import java.util.List;

public record SearchResultResponse(
        List<EstablishmentResultResponse> results,
        long totalHits,
        int page,
        int size
) {}
