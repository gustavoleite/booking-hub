package com.bookinghub.review.application.dto;

import java.util.UUID;

public record RatingStatsResponse(
        UUID subjectId,
        Double averageRating,
        long totalReviews
) {}
