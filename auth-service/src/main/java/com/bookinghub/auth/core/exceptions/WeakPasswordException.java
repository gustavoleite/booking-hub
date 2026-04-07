package com.bookinghub.auth.core.exceptions;

public class WeakPasswordException extends RuntimeException {
  public WeakPasswordException(String message) {
    super(message);
  }
}
