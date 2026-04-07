package com.bookinghub.booking.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
        UUID establishmentId,
        UUID professionalId,
        UUID providedServiceId,
        int durationMinutes,
        BigDecimal price,
        List<LocalDateTime> availableSlots
) {
}
