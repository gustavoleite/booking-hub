package com.bookinghub.review.core.exceptions;

public class BookingNotEligibleException extends RuntimeException {
    public BookingNotEligibleException(String message) {
        super(message);
    }
}
