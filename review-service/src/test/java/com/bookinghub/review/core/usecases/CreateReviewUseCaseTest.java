package com.bookinghub.review.core.usecases;

import com.bookinghub.review.core.domain.EligibleBooking;
import com.bookinghub.review.core.domain.Review;
import com.bookinghub.review.core.exceptions.BookingNotEligibleException;
import com.bookinghub.review.core.exceptions.ForbiddenReviewAccessException;
import com.bookinghub.review.core.exceptions.ReviewAlreadyExistsException;
import com.bookinghub.review.core.ports.EligibleBookingRepository;
import com.bookinghub.review.core.ports.ReviewEventPublisher;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateReviewUseCaseTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private EligibleBookingRepository eligibleBookingRepository;
    @Mock private ReviewEventPublisher eventPublisher;

    @InjectMocks
    private CreateReviewUseCase useCase;

    private final UUID bookingId = UUID.randomUUID();
    private final UUID professionalId = UUID.randomUUID();
    private final UUID establishmentId = UUID.randomUUID();
    private final String clientId = "client-1";

    private EligibleBooking eligible() {
        return new EligibleBooking(bookingId, clientId, professionalId, establishmentId, LocalDateTime.now());
    }

    @Test
    void shouldCreateReviewSuccessfully() {
        when(eligibleBookingRepository.findById(bookingId)).thenReturn(Optional.of(eligible()));
        when(reviewRepository.existsByBookingId(bookingId)).thenReturn(false);
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Review result = useCase.execute(clientId, bookingId, 5, 4, "Excellent!");

        assertThat(result.getProfessionalRating()).isEqualTo(5);
        assertThat(result.getEstablishmentRating()).isEqualTo(4);
        verify(eventPublisher).publishReviewCreated(result);
    }

    @Test
    void shouldThrowWhenBookingNotEligible() {
        when(eligibleBookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(clientId, bookingId, 5, null, null))
                .isInstanceOf(BookingNotEligibleException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenClientDoesNotOwnBooking() {
        when(eligibleBookingRepository.findById(bookingId)).thenReturn(Optional.of(eligible()));

        assertThatThrownBy(() -> useCase.execute("other-client", bookingId, 5, null, null))
                .isInstanceOf(ForbiddenReviewAccessException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenBookingAlreadyReviewed() {
        when(eligibleBookingRepository.findById(bookingId)).thenReturn(Optional.of(eligible()));
        when(reviewRepository.existsByBookingId(bookingId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(clientId, bookingId, 5, null, null))
                .isInstanceOf(ReviewAlreadyExistsException.class);

        verify(reviewRepository, never()).save(any());
    }
}
