package com.bookinghub.booking.core.exceptions;

public class ForbiddenBookingAccessException extends RuntimeException {
    public ForbiddenBookingAccessException(String message) { super(message); }
}
