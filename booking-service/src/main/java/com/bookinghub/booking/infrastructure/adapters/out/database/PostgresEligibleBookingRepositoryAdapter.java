package com.bookinghub.booking.infrastructure.adapters.out.database;

import com.bookinghub.booking.core.domain.EligibleBooking;
import com.bookinghub.booking.core.ports.EligibleBookingRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresEligibleBookingRepositoryAdapter implements EligibleBookingRepository {

  private final JpaEligibleBookingRepository jpa;

  @Override
  public void save(EligibleBooking eligibleBooking) {
    jpa.save(EligibleBookingJpaEntity.from(eligibleBooking));
  }

  @Override
  public Optional<EligibleBooking> findById(UUID bookingId) {
    return jpa.findById(bookingId).map(EligibleBookingJpaEntity::toDomain);
  }

  @Override
  public boolean existsById(UUID bookingId) {
    return jpa.existsById(bookingId);
  }
}
