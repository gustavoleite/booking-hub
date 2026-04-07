package com.bookinghub.booking.infrastructure.adapters.out.database;

import com.bookinghub.booking.core.domain.EligibleBooking;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_eligible_bookings")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EligibleBookingJpaEntity {

    @Id
    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(name = "establishment_id", nullable = false)
    private UUID establishmentId;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    public static EligibleBookingJpaEntity from(EligibleBooking domain) {
        return new EligibleBookingJpaEntity(
                domain.getBookingId(),
                domain.getClientId(),
                domain.getProfessionalId(),
                domain.getEstablishmentId(),
                domain.getCompletedAt());
    }

    public EligibleBooking toDomain() {
        return new EligibleBooking(
                bookingId, clientId, professionalId, establishmentId, completedAt);
    }
}
