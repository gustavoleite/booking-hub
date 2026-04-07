package com.bookinghub.booking.application.dto;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.domain.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        String clientId,
        UUID professionalId,
        UUID establishmentId,
        UUID providedServiceId,
        LocalDateTime startDatetime,
        LocalDateTime endDatetime,
        BookingStatus status,
        BigDecimal price,
        int durationMinutes,
        String notes,
        String cancelReason,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt
) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getClientId(),
                b.getProfessionalId(),
                b.getEstablishmentId(),
                b.getProvidedServiceId(),
                b.getStartDatetime(),
                b.getEndDatetime(),
                b.getStatus(),
                b.getPrice(),
                b.getDurationMinutes(),
                b.getNotes(),
                b.getCancelReason(),
                b.getCreatedAt(),
                b.getCancelledAt()
        );
    }
}
