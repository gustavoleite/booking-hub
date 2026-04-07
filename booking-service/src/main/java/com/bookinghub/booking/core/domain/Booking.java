package com.bookinghub.booking.core.domain;

import com.bookinghub.booking.core.exceptions.BookingStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Booking {
    private final UUID id;
    private final String clientId;
    private final UUID professionalId;
    private final UUID establishmentId;
    private final UUID providedServiceId;
    private final LocalDateTime startDatetime;
    private final LocalDateTime endDatetime;
    private BookingStatus status;
    private final BigDecimal price;
    private final int durationMinutes;
    private final String notes;
    private String cancelReason;
    private final LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    public static Booking create(String clientId, UUID professionalId, UUID establishmentId,
                               UUID providedServiceId, LocalDateTime startDatetime,
                               BigDecimal price, int durationMinutes, String notes) {
        return Booking.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .professionalId(professionalId)
                .establishmentId(establishmentId)
                .providedServiceId(providedServiceId)
                .startDatetime(startDatetime)
                .endDatetime(startDatetime.plusMinutes(durationMinutes))
                .status(BookingStatus.CONFIRMED)
                .price(price)
                .durationMinutes(durationMinutes)
                .notes(notes)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void cancel(String reason) {
        if (status != BookingStatus.CONFIRMED) {
            throw new BookingStatusException("Only CONFIRMED bookings can be cancelled");
        }
        this.status = BookingStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    public void complete() {
        if (status != BookingStatus.CONFIRMED) {
            throw new BookingStatusException("Only CONFIRMED bookings can be completed");
        }
        this.status = BookingStatus.COMPLETED;
    }

    public void markNoShow() {
        if (status != BookingStatus.CONFIRMED) {
            throw new BookingStatusException("Only CONFIRMED bookings can be marked as no-show");
        }
        this.status = BookingStatus.NO_SHOW;
    }

    public boolean isOwnedBy(String userId) {
        return this.clientId.equals(userId);
    }
}
