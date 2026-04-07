package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.exceptions.BookingNotFoundException;
import com.bookinghub.booking.core.exceptions.ForbiddenBookingAccessException;
import com.bookinghub.booking.core.ports.BookingRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MarkNoShowUseCase {

  private final BookingRepository bookingRepository;

  public Booking execute(UUID bookingId, String role) {
    if (!"ROLE_PROFESSIONAL".equals(role) && !"ROLE_OWNER".equals(role)) {
      throw new ForbiddenBookingAccessException("Only professionals or owners can mark a no-show");
    }

    Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

    booking.markNoShow();
    return bookingRepository.save(booking);
  }
}
