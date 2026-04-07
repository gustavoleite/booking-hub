package com.bookinghub.booking.application.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID professionalId,
        @NotNull UUID establishmentId,
        @NotNull UUID providedServiceId,
        @NotNull @Future LocalDateTime startDatetime,
        String notes
) {
}
