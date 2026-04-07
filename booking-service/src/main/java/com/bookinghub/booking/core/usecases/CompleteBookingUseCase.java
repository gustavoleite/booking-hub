package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.exceptions.BookingNotFoundException;
import com.bookinghub.booking.core.exceptions.ForbiddenBookingAccessException;
import com.bookinghub.booking.core.ports.BookingEventPublisher;
import com.bookinghub.booking.core.ports.BookingRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompleteBookingUseCase {

    private final BookingRepository bookingRepository;
    private final BookingEventPublisher eventPublisher;
    private final ConsumeBookingCompletedUseCase consumeBookingCompletedUseCase;

    public Booking execute(UUID bookingId, String role) {
        if (!"ROLE_PROFESSIONAL".equals(role) && !"ROLE_OWNER".equals(role)) {
            throw new ForbiddenBookingAccessException(
                    "Only professionals or owners can complete bookings");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        booking.complete();
        Booking saved = bookingRepository.save(booking);
        eventPublisher.publishBookingCompleted(saved);

        consumeBookingCompletedUseCase.execute(
                saved.getId(),
                saved.getClientId(),
                saved.getProfessionalId(),
                saved.getEstablishmentId(),
                LocalDateTime.now());

        return saved;
    }
}
