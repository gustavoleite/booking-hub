package com.bookinghub.booking.core.domain;

import com.bookinghub.booking.core.exceptions.InvalidReviewException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Review {

    private final UUID id;
    private final UUID bookingId;
    private final String clientId;
    private final UUID professionalId;
    private final UUID establishmentId;
    private final Integer professionalRating;
    private final Integer establishmentRating;
    private final String comment;
    private final LocalDateTime createdAt;

    public static Review create(UUID bookingId, String clientId, UUID professionalId,
                              UUID establishmentId, Integer professionalRating,
                              Integer establishmentRating, String comment) {
        if (professionalRating == null && establishmentRating == null) {
            throw new InvalidReviewException(
                    "At least one rating (professional or establishment) must be provided");
        }
        validateRating("professionalRating", professionalRating);
        validateRating("establishmentRating", establishmentRating);

        return Review.builder()
                .id(UUID.randomUUID())
                .bookingId(bookingId)
                .clientId(clientId)
                .professionalId(professionalId)
                .establishmentId(establishmentId)
                .professionalRating(professionalRating)
                .establishmentRating(establishmentRating)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static void validateRating(String field, Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new InvalidReviewException(field + " must be between 1 and 5");
        }
    }
}
