package com.bookinghub.booking.core.exceptions;

public class BookingNotEligibleException extends RuntimeException {
    public BookingNotEligibleException(String message) {
        super(message);
    }
}
