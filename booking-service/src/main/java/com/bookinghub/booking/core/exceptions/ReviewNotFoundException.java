package com.bookinghub.booking.core.exceptions;

public class ReviewNotFoundException extends RuntimeException {
  public ReviewNotFoundException(String message) {
    super(message);
  }
}
