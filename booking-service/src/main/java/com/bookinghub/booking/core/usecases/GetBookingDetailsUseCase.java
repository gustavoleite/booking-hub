package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.exceptions.BookingNotFoundException;
import com.bookinghub.booking.core.exceptions.ForbiddenBookingAccessException;
import com.bookinghub.booking.core.ports.BookingRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetBookingDetailsUseCase {

    private final BookingRepository bookingRepository;

    public Booking execute(UUID bookingId, String requestingUserId, String role) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        boolean isClient = booking.isOwnedBy(requestingUserId);
        boolean isProfessionalOrOwner =
                "ROLE_PROFESSIONAL".equals(role) || "ROLE_OWNER".equals(role);

        if (!isClient && !isProfessionalOrOwner) {
            throw new ForbiddenBookingAccessException("Access denied to this booking");
        }

        return booking;
    }
}
