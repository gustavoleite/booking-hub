package com.bookinghub.search.core.domain;

import lombok.*;
import java.util.List;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class SearchPage {
    private List<EstablishmentResult> results;
    private long totalHits;
    private int page;
    private int size;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EstablishmentResult {
        private EstablishmentDocument document;
        private Double distanceKm;
        private Double score;
    }
}
