package com.bookinghub.booking.application.dto;

import java.util.UUID;

public record RatingStatsResponse(
        UUID subjectId,
        Double averageRating,
        long totalReviews
) {
}
