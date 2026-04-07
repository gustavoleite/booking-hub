package com.bookinghub.auth.core.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExceptionsTest {

  @Test
  void testEmailAlreadyExistsException() {
    EmailAlreadyExistsException ex = new EmailAlreadyExistsException("message");
    assertEquals("message", ex.getMessage());
  }

  @Test
  void testInactiveUserException() {
    InactiveUserException ex = new InactiveUserException("message");
    assertEquals("message", ex.getMessage());
  }

  @Test
  void testInvalidCredentialsException() {
    InvalidCredentialsException ex = new InvalidCredentialsException("message");
    assertEquals("message", ex.getMessage());
  }

  @Test
  void testInvalidRoleException() {
    InvalidRoleException ex = new InvalidRoleException("message");
    assertEquals("message", ex.getMessage());
  }

  @Test
  void testWeakPasswordException() {
    WeakPasswordException ex = new WeakPasswordException("message");
    assertEquals("message", ex.getMessage());
  }
}
