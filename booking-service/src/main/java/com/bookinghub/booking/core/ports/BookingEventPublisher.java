package com.bookinghub.booking.core.ports;

import com.bookinghub.booking.core.domain.Booking;

public interface BookingEventPublisher {
    void publishBookingCreated(Booking booking);
    void publishBookingCancelled(Booking booking);
    void publishBookingCompleted(Booking booking);
}
