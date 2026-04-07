package com.bookinghub.gateway.core.domain.exceptions;

public class MissingTokenException extends UnauthorizedException {
  public MissingTokenException(String message) {
    super(message);
  }
}
