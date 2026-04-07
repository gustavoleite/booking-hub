package com.bookinghub.gateway.core.domain.exceptions;

public class JwtConfigurationException extends RuntimeException {
  public JwtConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  public JwtConfigurationException(String message) {
    super(message);
  }
}
