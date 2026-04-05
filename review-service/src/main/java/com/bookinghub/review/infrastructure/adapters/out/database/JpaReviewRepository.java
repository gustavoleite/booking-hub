package com.bookinghub.review.infrastructure.adapters.out.database;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaReviewRepository extends JpaRepository<ReviewEntity, UUID> {
    Optional<ReviewEntity> findByBookingId(UUID bookingId);
    boolean existsByBookingId(UUID bookingId);
    List<ReviewEntity> findByProfessionalIdOrderByCreatedAtDesc(UUID professionalId);
    List<ReviewEntity> findByEstablishmentIdOrderByCreatedAtDesc(UUID establishmentId);
}
