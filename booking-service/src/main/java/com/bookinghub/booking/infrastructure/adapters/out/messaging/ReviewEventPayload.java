package com.bookinghub.booking.infrastructure.adapters.out.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewEventPayload(
        UUID reviewId,
        UUID bookingId,
        String clientId,
        UUID professionalId,
        UUID establishmentId,
        Integer professionalRating,
        Integer establishmentRating,
        LocalDateTime occurredAt
) {
}
