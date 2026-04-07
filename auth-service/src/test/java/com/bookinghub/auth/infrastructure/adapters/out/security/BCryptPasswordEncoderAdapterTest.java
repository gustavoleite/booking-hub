package com.bookinghub.auth.infrastructure.adapters.out.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BCryptPasswordEncoderAdapterTest {

  private BCryptPasswordEncoderAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new BCryptPasswordEncoderAdapter();
  }

  @Test
  void shouldEncodePassword() {
    String raw = "password123";
    String encoded = adapter.encode(raw);

    assertNotNull(encoded);
    assertNotEquals(raw, encoded);
    assertTrue(adapter.matches(raw, encoded));
  }

  @Test
  void shouldNotMatchWrongPassword() {
    String raw = "password123";
    String encoded = adapter.encode(raw);

    assertFalse(adapter.matches("wrongpassword", encoded));
  }
}
