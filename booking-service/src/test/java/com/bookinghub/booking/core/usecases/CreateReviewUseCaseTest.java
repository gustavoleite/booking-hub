package com.bookinghub.booking.core.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.booking.core.domain.EligibleBooking;
import com.bookinghub.booking.core.domain.Review;
import com.bookinghub.booking.core.exceptions.BookingNotEligibleException;
import com.bookinghub.booking.core.exceptions.ForbiddenReviewAccessException;
import com.bookinghub.booking.core.exceptions.ReviewAlreadyExistsException;
import com.bookinghub.booking.core.ports.EligibleBookingRepository;
import com.bookinghub.booking.core.ports.ReviewEventPublisher;
import com.bookinghub.booking.core.ports.ReviewRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateReviewUseCaseTest {

  @Mock
  private ReviewRepository reviewRepository;
  @Mock
  private EligibleBookingRepository eligibleBookingRepository;
  @Mock
  private ReviewEventPublisher eventPublisher;

  @InjectMocks
  private CreateReviewUseCase useCase;

  @Test
  void shouldCreateReviewSuccessfully() {
    UUID bookingId = UUID.randomUUID();
    String clientId = "client-1";
    UUID profId = UUID.randomUUID();
    UUID estId = UUID.randomUUID();
    EligibleBooking eligible = new EligibleBooking(bookingId, clientId, profId, estId, LocalDateTime.now());

    when(eligibleBookingRepository.findById(bookingId)).thenReturn(Optional.of(eligible));
    when(reviewRepository.existsByBookingId(bookingId)).thenReturn(false);
    when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

    Review result = useCase.execute(clientId, bookingId, 5, 4, "Good service");

    assertThat(result).isNotNull();
    assertThat(result.getBookingId()).isEqualTo(bookingId);
    assertThat(result.getClientId()).isEqualTo(clientId);
    verify(eventPublisher).publishReviewCreated(any(Review.class));
  }

  @Test
  void shouldThrowWhenBookingNotEligible() {
    UUID bookingId = UUID.randomUUID();
    when(eligibleBookingRepository.findById(bookingId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute("c", bookingId, 5, 5, null))
        .isInstanceOf(BookingNotEligibleException.class);
  }

  @Test
  void shouldThrowWhenClientIsNotOwner() {
    UUID bookingId = UUID.randomUUID();
    EligibleBooking eligible = new EligibleBooking(bookingId, "other-client", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now());
    when(eligibleBookingRepository.findById(bookingId)).thenReturn(Optional.of(eligible));

    assertThatThrownBy(() -> useCase.execute("client-1", bookingId, 5, 5, null))
        .isInstanceOf(ForbiddenReviewAccessException.class);
  }

  @Test
  void shouldThrowWhenReviewAlreadyExists() {
    UUID bookingId = UUID.randomUUID();
    String clientId = "client-1";
    EligibleBooking eligible = new EligibleBooking(bookingId, clientId, UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now());
    when(eligibleBookingRepository.findById(bookingId)).thenReturn(Optional.of(eligible));
    when(reviewRepository.existsByBookingId(bookingId)).thenReturn(true);

    assertThatThrownBy(() -> useCase.execute(clientId, bookingId, 5, 5, null))
        .isInstanceOf(ReviewAlreadyExistsException.class);
  }
}
