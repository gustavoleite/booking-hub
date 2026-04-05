package com.bookinghub.review.infrastructure.adapters.out.database;

import com.bookinghub.review.core.domain.Review;
import com.bookinghub.review.core.ports.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostgresReviewRepositoryAdapter implements ReviewRepository {

    private final JpaReviewRepository jpaRepository;

    @Override
    public Review save(Review review) {
        return toDomain(jpaRepository.save(toEntity(review)));
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Review> findByBookingId(UUID bookingId) {
        return jpaRepository.findByBookingId(bookingId).map(this::toDomain);
    }

    @Override
    public boolean existsByBookingId(UUID bookingId) {
        return jpaRepository.existsByBookingId(bookingId);
    }

    @Override
    public List<Review> findByProfessionalId(UUID professionalId) {
        return jpaRepository.findByProfessionalIdOrderByCreatedAtDesc(professionalId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Review> findByEstablishmentId(UUID establishmentId) {
        return jpaRepository.findByEstablishmentIdOrderByCreatedAtDesc(establishmentId)
                .stream().map(this::toDomain).toList();
    }

    private Review toDomain(ReviewEntity e) {
        return Review.builder()
                .id(e.getId())
                .bookingId(e.getBookingId())
                .clientId(e.getClientId())
                .professionalId(e.getProfessionalId())
                .establishmentId(e.getEstablishmentId())
                .professionalRating(e.getProfessionalRating())
                .establishmentRating(e.getEstablishmentRating())
                .comment(e.getComment())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private ReviewEntity toEntity(Review r) {
        return ReviewEntity.builder()
                .id(r.getId())
                .bookingId(r.getBookingId())
                .clientId(r.getClientId())
                .professionalId(r.getProfessionalId())
                .establishmentId(r.getEstablishmentId())
                .professionalRating(r.getProfessionalRating())
                .establishmentRating(r.getEstablishmentRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
