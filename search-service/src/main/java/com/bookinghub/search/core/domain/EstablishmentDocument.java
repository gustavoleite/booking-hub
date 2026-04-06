package com.bookinghub.search.core.domain;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
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

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ServiceEntry {
        private String serviceId;
        private String title;
        private Double minPrice;
        private Double maxPrice;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProfessionalEntry {
        private String professionalId;
        private String name;
        private List<String> specialties;
    }
}
