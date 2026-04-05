package com.bookinghub.review.infrastructure.adapters.out.database;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoEligibleBookingRepository extends MongoRepository<EligibleBookingDocument, String> {
}
