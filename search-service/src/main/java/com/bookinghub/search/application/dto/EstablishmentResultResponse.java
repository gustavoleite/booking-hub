package com.bookinghub.search.application.dto;

import java.util.List;

public record EstablishmentResultResponse(
        String id,
        String name,
        String description,
        String city,
        String state,
        List<ServiceSummaryResponse> services,
        List<ProfessionalSummaryResponse> professionals,
        Double minPrice,
        Double maxPrice,
        Double averageRating,
        int totalReviews,
        Double distanceKm,
        Double score
) {
  public record ServiceSummaryResponse(String title, Double minPrice, Double maxPrice) {
  }

  public record ProfessionalSummaryResponse(String name, List<String> specialties) {
  }
}
