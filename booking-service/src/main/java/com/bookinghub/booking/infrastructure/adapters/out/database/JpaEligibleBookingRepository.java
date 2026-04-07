package com.bookinghub.booking.infrastructure.adapters.out.database;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEligibleBookingRepository
        extends JpaRepository<EligibleBookingJpaEntity, UUID> {
}
