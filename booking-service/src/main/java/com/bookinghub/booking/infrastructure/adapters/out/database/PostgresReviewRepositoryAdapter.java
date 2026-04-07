package com.bookinghub.booking.infrastructure.adapters.out.database;

import com.bookinghub.booking.core.domain.Review;
import com.bookinghub.booking.core.ports.ReviewRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresReviewRepositoryAdapter implements ReviewRepository {

  private final JpaReviewRepository jpa;

  @Override
  public Review save(Review review) {
    return jpa.save(ReviewJpaEntity.from(review)).toDomain();
  }

  @Override
  public Optional<Review> findById(UUID id) {
    return jpa.findById(id).map(ReviewJpaEntity::toDomain);
  }

  @Override
  public Optional<Review> findByBookingId(UUID bookingId) {
    return jpa.findByBookingId(bookingId).map(ReviewJpaEntity::toDomain);
  }

  @Override
  public boolean existsByBookingId(UUID bookingId) {
    return jpa.existsByBookingId(bookingId);
  }

  @Override
  public List<Review> findByProfessionalId(UUID professionalId) {
    return jpa.findByProfessionalId(professionalId).stream()
        .map(ReviewJpaEntity::toDomain)
        .toList();
  }

  @Override
  public List<Review> findByEstablishmentId(UUID establishmentId) {
    return jpa.findByEstablishmentId(establishmentId).stream()
        .map(ReviewJpaEntity::toDomain)
        .toList();
  }
}
