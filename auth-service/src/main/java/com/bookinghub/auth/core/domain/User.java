package com.bookinghub.auth.core.domain;

import java.util.Set;
import java.util.UUID;

public class User {
  private UUID id;
  private String email;
  private String passwordHash;
  private Set<Role> roles;
  private boolean active;

  public User(UUID id, String email, String passwordHash, Set<Role> roles, boolean active) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.roles = roles;
    this.active = active;
  }

  // Getters
  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public boolean isActive() {
    return active;
  }
}
