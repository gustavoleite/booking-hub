package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.ports.BookingRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListEstablishmentBookingsUseCase {

    private final BookingRepository bookingRepository;

    public List<Booking> execute(UUID establishmentId) {
        return bookingRepository.findByEstablishmentId(establishmentId);
    }
}
