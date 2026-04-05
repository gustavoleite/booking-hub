package com.bookinghub.review.infrastructure.adapters.out.database;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_eligible_bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibleBookingEntity {

    @Id
    private UUID bookingId;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private UUID professionalId;

    @Column(nullable = false)
    private UUID establishmentId;

    @Column(nullable = false)
    private LocalDateTime completedAt;
}
