package com.bookinghub.review.application.dto;

import com.bookinghub.review.core.domain.Review;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID bookingId,
        String clientId,
        UUID professionalId,
        UUID establishmentId,
        Integer professionalRating,
        Integer establishmentRating,
        String comment,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review r) {
        return new ReviewResponse(r.getId(), r.getBookingId(), r.getClientId(),
                r.getProfessionalId(), r.getEstablishmentId(),
                r.getProfessionalRating(), r.getEstablishmentRating(),
                r.getComment(), r.getCreatedAt());
    }
}
