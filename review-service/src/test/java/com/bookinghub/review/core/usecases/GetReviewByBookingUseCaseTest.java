package com.bookinghub.review.core.usecases;

import com.bookinghub.review.core.domain.Review;
import com.bookinghub.review.core.exceptions.ForbiddenReviewAccessException;
import com.bookinghub.review.core.exceptions.ReviewNotFoundException;
import com.bookinghub.review.core.ports.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetReviewByBookingUseCaseTest {

    @Mock private ReviewRepository reviewRepository;

    @InjectMocks
    private GetReviewByBookingUseCase useCase;

    private final UUID bookingId = UUID.randomUUID();
    private final String clientId = "client-1";

    private Review buildReview() {
        return Review.builder()
                .id(UUID.randomUUID())
                .bookingId(bookingId)
                .clientId(clientId)
                .professionalId(UUID.randomUUID())
                .establishmentId(UUID.randomUUID())
                .professionalRating(5)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldReturnReviewForOwnerClient() {
        when(reviewRepository.findByBookingId(bookingId)).thenReturn(Optional.of(buildReview()));

        Review result = useCase.execute(bookingId, clientId, "ROLE_CLIENT");

        assertThat(result.getBookingId()).isEqualTo(bookingId);
    }

    @Test
    void shouldReturnReviewForProfessional() {
        when(reviewRepository.findByBookingId(bookingId)).thenReturn(Optional.of(buildReview()));

        Review result = useCase.execute(bookingId, "any-professional", "ROLE_PROFESSIONAL");

        assertThat(result).isNotNull();
    }

    @Test
    void shouldReturnReviewForOwner() {
        when(reviewRepository.findByBookingId(bookingId)).thenReturn(Optional.of(buildReview()));

        Review result = useCase.execute(bookingId, "any-owner", "ROLE_OWNER");

        assertThat(result).isNotNull();
    }

    @Test
    void shouldThrowForbiddenForOtherClient() {
        when(reviewRepository.findByBookingId(bookingId)).thenReturn(Optional.of(buildReview()));

        assertThatThrownBy(() -> useCase.execute(bookingId, "other-client", "ROLE_CLIENT"))
                .isInstanceOf(ForbiddenReviewAccessException.class);
    }

    @Test
    void shouldThrowNotFoundWhenReviewDoesNotExist() {
        when(reviewRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(bookingId, clientId, "ROLE_CLIENT"))
                .isInstanceOf(ReviewNotFoundException.class);
    }
}
