package com.bookinghub.review.core.domain;

import com.bookinghub.review.core.exceptions.InvalidReviewException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();
    private static final String CLIENT_ID = "client-1";
    private static final UUID PROFESSIONAL_ID = UUID.randomUUID();
    private static final UUID ESTABLISHMENT_ID = UUID.randomUUID();

    @Test
    void shouldCreateReviewWithBothRatings() {
        Review review = Review.create(BOOKING_ID, CLIENT_ID, PROFESSIONAL_ID, ESTABLISHMENT_ID, 5, 4, "Great!");
        assertThat(review.getId()).isNotNull();
        assertThat(review.getProfessionalRating()).isEqualTo(5);
        assertThat(review.getEstablishmentRating()).isEqualTo(4);
        assertThat(review.getComment()).isEqualTo("Great!");
        assertThat(review.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldCreateReviewWithOnlyProfessionalRating() {
        Review review = Review.create(BOOKING_ID, CLIENT_ID, PROFESSIONAL_ID, ESTABLISHMENT_ID, 3, null, null);
        assertThat(review.getProfessionalRating()).isEqualTo(3);
        assertThat(review.getEstablishmentRating()).isNull();
    }

    @Test
    void shouldCreateReviewWithOnlyEstablishmentRating() {
        Review review = Review.create(BOOKING_ID, CLIENT_ID, PROFESSIONAL_ID, ESTABLISHMENT_ID, null, 5, null);
        assertThat(review.getProfessionalRating()).isNull();
        assertThat(review.getEstablishmentRating()).isEqualTo(5);
    }

    @Test
    void shouldThrowWhenNoRatingsProvided() {
        assertThatThrownBy(() ->
                Review.create(BOOKING_ID, CLIENT_ID, PROFESSIONAL_ID, ESTABLISHMENT_ID, null, null, "comment"))
                .isInstanceOf(InvalidReviewException.class)
                .hasMessageContaining("At least one rating");
    }

    @Test
    void shouldThrowWhenProfessionalRatingOutOfRange() {
        assertThatThrownBy(() ->
                Review.create(BOOKING_ID, CLIENT_ID, PROFESSIONAL_ID, ESTABLISHMENT_ID, 6, null, null))
                .isInstanceOf(InvalidReviewException.class)
                .hasMessageContaining("professionalRating");
    }

    @Test
    void shouldThrowWhenEstablishmentRatingBelowMin() {
        assertThatThrownBy(() ->
                Review.create(BOOKING_ID, CLIENT_ID, PROFESSIONAL_ID, ESTABLISHMENT_ID, null, 0, null))
                .isInstanceOf(InvalidReviewException.class)
                .hasMessageContaining("establishmentRating");
    }
}
