package com.bookinghub.auth.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void shouldCreateUserCorrectly() {
    UUID id = UUID.randomUUID();
    String email = "test@example.com";
    String passwordHash = "hashed";
    Set<Role> roles = Set.of(Role.ROLE_CLIENT);
    boolean active = true;

    User user = new User(id, email, passwordHash, roles, active);

    assertEquals(id, user.getId());
    assertEquals(email, user.getEmail());
    assertEquals(passwordHash, user.getPasswordHash());
    assertEquals(roles, user.getRoles());
    assertTrue(user.isActive());
  }
}
