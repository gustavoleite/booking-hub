package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.exceptions.BookingNotFoundException;
import com.bookinghub.booking.core.exceptions.ForbiddenBookingAccessException;
import com.bookinghub.booking.core.ports.BookingEventPublisher;
import com.bookinghub.booking.core.ports.BookingRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CompleteBookingUseCase {

    private final BookingRepository bookingRepository;
    private final BookingEventPublisher eventPublisher;

    public Booking execute(UUID bookingId, String role) {
        if (!"ROLE_PROFESSIONAL".equals(role) && !"ROLE_OWNER".equals(role)) {
            throw new ForbiddenBookingAccessException("Only professionals or owners can complete bookings");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        booking.complete();
        Booking saved = bookingRepository.save(booking);
        eventPublisher.publishBookingCompleted(saved);
        return saved;
    }
}
