package com.bookinghub.review.infrastructure.adapters.out.database;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaEligibleBookingRepository extends JpaRepository<EligibleBookingEntity, UUID> {
}
