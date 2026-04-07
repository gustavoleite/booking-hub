package com.bookinghub.catalog.core.exceptions;

public class ProfessionalNotFoundException extends NotFoundException {
  public ProfessionalNotFoundException(String message) {
    super(message);
  }

  public ProfessionalNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
