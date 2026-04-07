package com.bookinghub.catalog.core.domain;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Professional {
  private final UUID id; // Same ID as Auth Service
  private String name;
  private String bio;
  private String avatarUrl;
  @Builder.Default
  private boolean active = true;
  private List<String> specialties;

  public void updateProfile(String name, String bio, String avatarUrl, List<String> specialties) {
    this.name = name;
    this.bio = bio;
    this.avatarUrl = avatarUrl;
    this.specialties = specialties;
  }

  public void inactivate() {
    this.active = false;
  }
}
