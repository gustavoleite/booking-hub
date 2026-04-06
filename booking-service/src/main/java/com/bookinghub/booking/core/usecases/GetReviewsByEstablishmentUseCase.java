package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Review;
import com.bookinghub.booking.core.ports.ReviewRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

@RequiredArgsConstructor
public class GetReviewsByEstablishmentUseCase {

    private final ReviewRepository reviewRepository;

    public record Result(List<Review> reviews, Double averageRating, long totalReviews) {}

    public Result execute(UUID establishmentId) {
        List<Review> reviews = reviewRepository.findByEstablishmentId(establishmentId);
        OptionalDouble avg = reviews.stream()
                .filter(r -> r.getEstablishmentRating() != null)
                .mapToInt(Review::getEstablishmentRating)
                .average();
        long count = reviews.stream().filter(r -> r.getEstablishmentRating() != null).count();
        Double averageRating = avg.isPresent()
                ? Math.round(avg.getAsDouble() * 10.0) / 10.0
                : null;
        return new Result(reviews, averageRating, count);
    }
}
