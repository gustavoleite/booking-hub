package com.bookinghub.booking.core.ports;

import com.bookinghub.booking.core.domain.Review;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {
  Review save(Review review);

  Optional<Review> findById(UUID id);

  Optional<Review> findByBookingId(UUID bookingId);

  boolean existsByBookingId(UUID bookingId);

  List<Review> findByProfessionalId(UUID professionalId);

  List<Review> findByEstablishmentId(UUID establishmentId);
}
