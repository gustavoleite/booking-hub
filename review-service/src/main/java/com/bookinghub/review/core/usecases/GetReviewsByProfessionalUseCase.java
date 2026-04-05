package com.bookinghub.review.core.usecases;

import com.bookinghub.review.core.domain.Review;
import com.bookinghub.review.core.ports.ReviewRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

@RequiredArgsConstructor
public class GetReviewsByProfessionalUseCase {

    private final ReviewRepository reviewRepository;

    public record Result(List<Review> reviews, Double averageRating, long totalReviews) {}

    public Result execute(UUID professionalId) {
        List<Review> reviews = reviewRepository.findByProfessionalId(professionalId);
        OptionalDouble avg = reviews.stream()
                .filter(r -> r.getProfessionalRating() != null)
                .mapToInt(Review::getProfessionalRating)
                .average();
        long count = reviews.stream().filter(r -> r.getProfessionalRating() != null).count();
        Double averageRating = avg.isPresent()
                ? Math.round(avg.getAsDouble() * 10.0) / 10.0
                : null;
        return new Result(reviews, averageRating, count);
    }
}
