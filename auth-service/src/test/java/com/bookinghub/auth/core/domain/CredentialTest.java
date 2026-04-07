package com.bookinghub.auth.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CredentialTest {

  @Test
  void shouldCreateCredentialWithValidInputs() {
    String email = "test@example.com";
    String password = "password123";

    Credential credential = new Credential(email, password);

    assertEquals(email, credential.email());
    assertEquals(password, credential.password());
  }

  @Test
  void shouldThrowExceptionWhenEmailIsNull() {
    assertThrows(IllegalArgumentException.class, () -> new Credential(null, "password123"));
  }

  @Test
  void shouldThrowExceptionWhenEmailIsInvalid() {
    assertThrows(IllegalArgumentException.class, () -> new Credential("invalid-email", "password123"));
  }

  @Test
  void shouldThrowExceptionWhenPasswordIsNull() {
    assertThrows(IllegalArgumentException.class, () -> new Credential("test@example.com", null));
  }

  @Test
  void shouldThrowExceptionWhenPasswordIsTooShort() {
    assertThrows(IllegalArgumentException.class, () -> new Credential("test@example.com", "short"));
  }
}
