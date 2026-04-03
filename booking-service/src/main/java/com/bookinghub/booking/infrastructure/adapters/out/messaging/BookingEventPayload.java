package com.bookinghub.booking.infrastructure.adapters.out.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingEventPayload(
        UUID bookingId,
        String clientId,
        UUID professionalId,
        UUID establishmentId,
        UUID providedServiceId,
        LocalDateTime startDatetime,
        LocalDateTime endDatetime,
        BigDecimal price,
        int durationMinutes,
        String status,
        LocalDateTime occurredAt
) {}
