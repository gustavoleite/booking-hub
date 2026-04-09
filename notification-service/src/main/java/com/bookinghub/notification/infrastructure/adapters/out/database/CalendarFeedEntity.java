package com.bookinghub.notification.infrastructure.adapters.out.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "calendar_feeds")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarFeedEntity {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false, unique = true)
  private String userId;

  @Column(name = "feed_token", nullable = false, unique = true)
  private String feedToken;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
