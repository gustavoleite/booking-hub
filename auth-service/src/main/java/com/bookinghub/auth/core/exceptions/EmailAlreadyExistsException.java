package com.bookinghub.auth.core.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
  public EmailAlreadyExistsException(String message) {
    super(message);
  }
}
