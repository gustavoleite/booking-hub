package com.bookinghub.search.core.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstablishmentDocument {
    private String id;
    private String name;
    private String description;
    private String city;
    private String state;
    private String zipCode;
    private Double lat;
    private Double lon;
    private List<ServiceEntry> services;
    private List<ProfessionalEntry> professionals;
    private Double minPrice;
    private Double maxPrice;
    private Double ratingSum;
    private Double averageRating;
    private Integer totalReviews;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceEntry {
        private String serviceId;
        private String title;
        private Double minPrice;
        private Double maxPrice;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfessionalEntry {
        private String professionalId;
        private String name;
        private List<String> specialties;
    }
}
