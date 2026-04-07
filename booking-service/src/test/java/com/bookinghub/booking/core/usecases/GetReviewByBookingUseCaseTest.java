package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Review;
import com.bookinghub.booking.core.exceptions.ForbiddenReviewAccessException;
import com.bookinghub.booking.core.exceptions.ReviewNotFoundException;
import com.bookinghub.booking.core.ports.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetReviewByBookingUseCaseTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private GetReviewByBookingUseCase useCase;

    @Test
    void shouldReturnReviewForOwner() {
        UUID bookingId = UUID.randomUUID();
        Review review = Review.builder().clientId("client-1").build();
        when(reviewRepository.findByBookingId(bookingId)).thenReturn(Optional.of(review));

        Review result = useCase.execute(bookingId, "any", "ROLE_OWNER");

        assertThat(result).isEqualTo(review);
    }

    @Test
    void shouldReturnReviewForClientOwner() {
        UUID bookingId = UUID.randomUUID();
        Review review = Review.builder().clientId("client-1").build();
        when(reviewRepository.findByBookingId(bookingId)).thenReturn(Optional.of(review));

        Review result = useCase.execute(bookingId, "client-1", "ROLE_CLIENT");

        assertThat(result).isEqualTo(review);
    }

    @Test
    void shouldThrowWhenNotAuthorized() {
        UUID bookingId = UUID.randomUUID();
        Review review = Review.builder().clientId("client-1").build();
        when(reviewRepository.findByBookingId(bookingId)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> useCase.execute(bookingId, "other-client", "ROLE_CLIENT"))
                .isInstanceOf(ForbiddenReviewAccessException.class);
    }

    @Test
    void shouldThrowWhenReviewNotFound() {
        UUID bookingId = UUID.randomUUID();
        when(reviewRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(bookingId, "any", "ROLE_OWNER"))
                .isInstanceOf(ReviewNotFoundException.class);
    }
}
