package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.exceptions.BookingNotFoundException;
import com.bookinghub.booking.core.exceptions.ForbiddenBookingAccessException;
import com.bookinghub.booking.core.ports.BookingEventPublisher;
import com.bookinghub.booking.core.ports.BookingRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CancelBookingUseCase {

  private final BookingRepository bookingRepository;
  private final BookingEventPublisher eventPublisher;

  public Booking execute(UUID bookingId, String requestingUserId, String role, String reason) {
    Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

    boolean isClient = "ROLE_CLIENT".equals(role) && booking.isOwnedBy(requestingUserId);
    boolean isOwner = "ROLE_OWNER".equals(role);

    if (!isClient && !isOwner) {
      throw new ForbiddenBookingAccessException(
          "Only the client or establishment owner can cancel this booking");
    }

    booking.cancel(reason);
    Booking saved = bookingRepository.save(booking);
    eventPublisher.publishBookingCancelled(saved);
    return saved;
  }
}
