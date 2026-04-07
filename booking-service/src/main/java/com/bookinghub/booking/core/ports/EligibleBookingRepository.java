package com.bookinghub.booking.core.ports;

import com.bookinghub.booking.core.domain.EligibleBooking;
import java.util.Optional;
import java.util.UUID;

public interface EligibleBookingRepository {
  void save(EligibleBooking eligibleBooking);

  Optional<EligibleBooking> findById(UUID bookingId);

  boolean existsById(UUID bookingId);
}
