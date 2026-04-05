package com.bookinghub.review.infrastructure.adapters.out.database;

import com.bookinghub.review.core.domain.EligibleBooking;
import com.bookinghub.review.core.ports.EligibleBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostgresEligibleBookingRepositoryAdapter implements EligibleBookingRepository {

    private final JpaEligibleBookingRepository jpaRepository;

    @Override
    public void save(EligibleBooking eligible) {
        jpaRepository.save(toEntity(eligible));
    }

    @Override
    public Optional<EligibleBooking> findById(UUID bookingId) {
        return jpaRepository.findById(bookingId).map(this::toDomain);
    }

    @Override
    public boolean existsById(UUID bookingId) {
        return jpaRepository.existsById(bookingId);
    }

    private EligibleBooking toDomain(EligibleBookingEntity e) {
        return new EligibleBooking(e.getBookingId(), e.getClientId(),
                e.getProfessionalId(), e.getEstablishmentId(), e.getCompletedAt());
    }

    private EligibleBookingEntity toEntity(EligibleBooking e) {
        return EligibleBookingEntity.builder()
                .bookingId(e.getBookingId())
                .clientId(e.getClientId())
                .professionalId(e.getProfessionalId())
                .establishmentId(e.getEstablishmentId())
                .completedAt(e.getCompletedAt())
                .build();
    }
}
