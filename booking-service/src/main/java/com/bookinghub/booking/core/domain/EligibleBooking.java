package com.bookinghub.booking.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class EligibleBooking {
    private final UUID bookingId;
    private final String clientId;
    private final UUID professionalId;
    private final UUID establishmentId;
    private final LocalDateTime completedAt;
}
