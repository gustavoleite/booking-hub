package com.bookinghub.review.infrastructure.adapters.out.database;

import com.bookinghub.review.core.domain.EligibleBooking;
import com.bookinghub.review.core.ports.EligibleBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MongoEligibleBookingRepositoryAdapter implements EligibleBookingRepository {

    private final MongoEligibleBookingRepository mongoRepository;

    @Override
    public void save(EligibleBooking eligible) {
        mongoRepository.save(toDocument(eligible));
    }

    @Override
    public Optional<EligibleBooking> findById(UUID bookingId) {
        return mongoRepository.findById(bookingId.toString()).map(this::toDomain);
    }

    @Override
    public boolean existsById(UUID bookingId) {
        return mongoRepository.existsById(bookingId.toString());
    }

    private EligibleBooking toDomain(EligibleBookingDocument d) {
        return new EligibleBooking(
                UUID.fromString(d.getBookingId()),
                d.getClientId(),
                UUID.fromString(d.getProfessionalId()),
                UUID.fromString(d.getEstablishmentId()),
                d.getCompletedAt());
    }

    private EligibleBookingDocument toDocument(EligibleBooking e) {
        return EligibleBookingDocument.builder()
                .bookingId(e.getBookingId().toString())
                .clientId(e.getClientId())
                .professionalId(e.getProfessionalId().toString())
                .establishmentId(e.getEstablishmentId().toString())
                .completedAt(e.getCompletedAt())
                .build();
    }
}
