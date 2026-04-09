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
import lombok.Setter;

@Entity
@Table(name = "booking_snapshots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSnapshotEntity {

  @Id
  @Column(name = "booking_id")
  private UUID bookingId;

  @Column(name = "client_id", nullable = false)
  private String clientId;

  @Column(name = "professional_id", nullable = false)
  private UUID professionalId;

  @Column(name = "start_datetime", nullable = false)
  private LocalDateTime startDatetime;

  @Column(name = "end_datetime", nullable = false)
  private LocalDateTime endDatetime;

  @Column(nullable = false)
  private String status;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
