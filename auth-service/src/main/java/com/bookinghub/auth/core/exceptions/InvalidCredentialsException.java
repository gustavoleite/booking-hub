package com.bookinghub.auth.core.exceptions;

public class InvalidCredentialsException extends RuntimeException {
  public InvalidCredentialsException(String message) {
    super(message);
  }
}
