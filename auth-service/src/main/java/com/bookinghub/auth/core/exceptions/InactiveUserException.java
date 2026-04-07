package com.bookinghub.auth.core.exceptions;

public class InactiveUserException extends RuntimeException {
  public InactiveUserException(String message) {
    super(message);
  }
}
