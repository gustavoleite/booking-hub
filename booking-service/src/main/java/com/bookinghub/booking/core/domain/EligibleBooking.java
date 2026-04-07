package com.bookinghub.booking.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EligibleBooking {
  private final UUID bookingId;
  private final String clientId;
  private final UUID professionalId;
  private final UUID establishmentId;
  private final LocalDateTime completedAt;
}
