package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Review;
import com.bookinghub.booking.core.exceptions.ForbiddenReviewAccessException;
import com.bookinghub.booking.core.exceptions.ReviewNotFoundException;
import com.bookinghub.booking.core.ports.ReviewRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetReviewByBookingUseCase {

    private final ReviewRepository reviewRepository;

    public Review execute(UUID bookingId, String requesterId, String requesterRole) {
        Review review = reviewRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Review not found for booking " + bookingId));

        boolean isOwner = "ROLE_OWNER".equals(requesterRole);
        boolean isProfessional = "ROLE_PROFESSIONAL".equals(requesterRole);
        boolean isClient = review.getClientId().equals(requesterId);

        if (!isOwner && !isProfessional && !isClient) {
            throw new ForbiddenReviewAccessException(
                    "You do not have permission to view this review");
        }
        return review;
    }
}
