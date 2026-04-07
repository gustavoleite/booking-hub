package com.bookinghub.gateway.core.domain.exceptions;

public class InvalidTokenException extends UnauthorizedException {
  public InvalidTokenException(String message) {
    super(message);
  }

  public InvalidTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
