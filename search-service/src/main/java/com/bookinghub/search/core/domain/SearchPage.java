package com.bookinghub.search.core.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchPage {
    private List<EstablishmentResult> results;
    private long totalHits;
    private int page;
    private int size;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstablishmentResult {
        private EstablishmentDocument document;
        private Double distanceKm;
        private Double score;
    }
}
