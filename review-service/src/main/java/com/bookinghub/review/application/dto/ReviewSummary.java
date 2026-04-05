package com.bookinghub.review.application.dto;

import com.bookinghub.review.core.domain.Review;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewSummary(
        UUID id,
        Integer professionalRating,
        Integer establishmentRating,
        String comment,
        LocalDateTime createdAt
) {
    public static ReviewSummary from(Review r) {
        return new ReviewSummary(r.getId(), r.getProfessionalRating(),
                r.getEstablishmentRating(), r.getComment(), r.getCreatedAt());
    }
}
