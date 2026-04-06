package com.bookinghub.booking.application.dto;

import java.util.List;

public record ReviewListResponse(
        List<ReviewSummary> reviews,
        Double averageRating,
        long totalReviews
) {}
