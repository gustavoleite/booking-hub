package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.ports.BookingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListClientBookingsUseCase {

    private final BookingRepository bookingRepository;

    public List<Booking> execute(String clientId) {
        return bookingRepository.findByClientId(clientId);
    }
}
