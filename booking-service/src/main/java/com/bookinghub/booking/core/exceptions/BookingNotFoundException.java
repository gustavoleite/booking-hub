package com.bookinghub.booking.core.exceptions;

public class BookingNotFoundException extends RuntimeException {
  public BookingNotFoundException(String message) {
    super(message);
  }
}
