package com.bookinghub.gateway.core.domain.exceptions;

public abstract class UnauthorizedException extends RuntimeException {
  protected UnauthorizedException(String message) {
    super(message);
  }

  protected UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
