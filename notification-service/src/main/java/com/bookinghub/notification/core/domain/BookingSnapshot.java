package com.bookinghub.notification.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BookingSnapshot {

  private final UUID bookingId;
  private final String clientId;
  private final UUID professionalId;
  private final LocalDateTime startDatetime;
  private final LocalDateTime endDatetime;
  private String status;
  private LocalDateTime updatedAt;

  public void updateStatus(String newStatus) {
    this.status = newStatus;
    this.updatedAt = LocalDateTime.now();
  }
}
