package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.EligibleBooking;
import com.bookinghub.booking.core.domain.Review;
import com.bookinghub.booking.core.exceptions.BookingNotEligibleException;
import com.bookinghub.booking.core.exceptions.ForbiddenReviewAccessException;
import com.bookinghub.booking.core.exceptions.ReviewAlreadyExistsException;
import com.bookinghub.booking.core.ports.EligibleBookingRepository;
import com.bookinghub.booking.core.ports.ReviewEventPublisher;
import com.bookinghub.booking.core.ports.ReviewRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateReviewUseCase {

  private final ReviewRepository reviewRepository;
  private final EligibleBookingRepository eligibleBookingRepository;
  private final ReviewEventPublisher eventPublisher;

  public Review execute(String clientId, UUID bookingId, Integer professionalRating,
                          Integer establishmentRating, String comment) {

    EligibleBooking eligible = eligibleBookingRepository.findById(bookingId)
        .orElseThrow(() -> new BookingNotEligibleException(
            "Booking not found or not yet completed"));

    if (!eligible.getClientId().equals(clientId)) {
      throw new ForbiddenReviewAccessException(
          "You can only review bookings that belong to you");
    }

    if (reviewRepository.existsByBookingId(bookingId)) {
      throw new ReviewAlreadyExistsException(
          "This booking has already been reviewed");
    }

    Review review = Review.create(bookingId, clientId, eligible.getProfessionalId(),
        eligible.getEstablishmentId(), professionalRating, establishmentRating, comment);

    Review saved = reviewRepository.save(review);
    eventPublisher.publishReviewCreated(saved);
    return saved;
  }
}
