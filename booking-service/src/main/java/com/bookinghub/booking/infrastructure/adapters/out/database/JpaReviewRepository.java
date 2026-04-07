package com.bookinghub.booking.infrastructure.adapters.out.database;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReviewRepository extends JpaRepository<ReviewJpaEntity, UUID> {
    Optional<ReviewJpaEntity> findByBookingId(UUID bookingId);

    boolean existsByBookingId(UUID bookingId);

    List<ReviewJpaEntity> findByProfessionalId(UUID professionalId);

    List<ReviewJpaEntity> findByEstablishmentId(UUID establishmentId);
}
