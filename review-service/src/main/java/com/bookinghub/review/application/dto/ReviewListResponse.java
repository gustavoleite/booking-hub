package com.bookinghub.review.application.dto;

import java.util.List;

public record ReviewListResponse(
        List<ReviewSummary> reviews,
        Double averageRating,
        long totalReviews
) {}
