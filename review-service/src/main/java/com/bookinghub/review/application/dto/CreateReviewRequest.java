package com.bookinghub.review.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReviewRequest(
        @NotNull UUID bookingId,
        Integer professionalRating,
        Integer establishmentRating,
        String comment
) {}
