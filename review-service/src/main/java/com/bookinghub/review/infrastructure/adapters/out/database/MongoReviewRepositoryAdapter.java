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
public class MongoReviewRepositoryAdapter implements ReviewRepository {

    private final MongoReviewRepository mongoRepository;

    @Override
    public Review save(Review review) {
        return toDomain(mongoRepository.save(toDocument(review)));
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return mongoRepository.findById(id.toString()).map(this::toDomain);
    }

    @Override
    public Optional<Review> findByBookingId(UUID bookingId) {
        return mongoRepository.findByBookingId(bookingId.toString()).map(this::toDomain);
    }

    @Override
    public boolean existsByBookingId(UUID bookingId) {
        return mongoRepository.existsByBookingId(bookingId.toString());
    }

    @Override
    public List<Review> findByProfessionalId(UUID professionalId) {
        return mongoRepository.findByProfessionalIdOrderByCreatedAtDesc(professionalId.toString())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Review> findByEstablishmentId(UUID establishmentId) {
        return mongoRepository.findByEstablishmentIdOrderByCreatedAtDesc(establishmentId.toString())
                .stream().map(this::toDomain).toList();
    }

    private Review toDomain(ReviewDocument d) {
        return Review.builder()
                .id(UUID.fromString(d.getId()))
                .bookingId(UUID.fromString(d.getBookingId()))
                .clientId(d.getClientId())
                .professionalId(UUID.fromString(d.getProfessionalId()))
                .establishmentId(UUID.fromString(d.getEstablishmentId()))
                .professionalRating(d.getProfessionalRating())
                .establishmentRating(d.getEstablishmentRating())
                .comment(d.getComment())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private ReviewDocument toDocument(Review r) {
        return ReviewDocument.builder()
                .id(r.getId().toString())
                .bookingId(r.getBookingId().toString())
                .clientId(r.getClientId())
                .professionalId(r.getProfessionalId().toString())
                .establishmentId(r.getEstablishmentId().toString())
                .professionalRating(r.getProfessionalRating())
                .establishmentRating(r.getEstablishmentRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
