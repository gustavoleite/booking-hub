package com.bookinghub.review.infrastructure.adapters.out.database;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoReviewRepository extends MongoRepository<ReviewDocument, String> {
    Optional<ReviewDocument> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
    List<ReviewDocument> findByProfessionalIdOrderByCreatedAtDesc(String professionalId);
    List<ReviewDocument> findByEstablishmentIdOrderByCreatedAtDesc(String establishmentId);
}
