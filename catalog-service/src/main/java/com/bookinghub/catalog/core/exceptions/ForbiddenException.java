package com.bookinghub.catalog.core.exceptions;

public class ForbiddenException extends RuntimeException {
  public ForbiddenException(String message) {
    super(message);
  }
}
