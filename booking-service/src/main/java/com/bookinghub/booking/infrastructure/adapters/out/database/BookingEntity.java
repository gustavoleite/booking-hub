package com.bookinghub.booking.infrastructure.adapters.out.database;

import com.bookinghub.booking.core.domain.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private UUID professionalId;

    @Column(nullable = false)
    private UUID establishmentId;

    @Column(nullable = false)
    private UUID providedServiceId;

    @Column(nullable = false)
    private LocalDateTime startDatetime;

    @Column(nullable = false)
    private LocalDateTime endDatetime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int durationMinutes;

    private String notes;
    private String cancelReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime cancelledAt;
}
