package com.bookinghub.auth.core.domain;

public record Credential(String email, String password) {
  public Credential {
    if (email == null || !email.contains("@")) {
      throw new IllegalArgumentException("Invalid email format");
    }
    if (password == null || password.length() < 8) {
      throw new IllegalArgumentException("Password must be at least 8 characters long");
    }
  }
}
