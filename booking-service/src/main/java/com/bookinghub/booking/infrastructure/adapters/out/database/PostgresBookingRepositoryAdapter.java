package com.bookinghub.booking.infrastructure.adapters.out.database;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.ports.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostgresBookingRepositoryAdapter implements BookingRepository {

    private final JpaBookingRepository jpaRepository;

    @Override
    public Booking save(Booking booking) {
        return toDomain(jpaRepository.save(toEntity(booking)));
    }

    @Override
    public Optional<Booking> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsActiveSlot(UUID professionalId, LocalDateTime startDatetime) {
        return jpaRepository.existsActiveSlot(professionalId, startDatetime);
    }

    @Override
    public List<Booking> findByClientId(String clientId) {
        return jpaRepository.findByClientIdOrderByStartDatetimeDesc(clientId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Booking> findByProfessionalId(UUID professionalId) {
        return jpaRepository.findByProfessionalIdOrderByStartDatetimeDesc(professionalId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Booking> findByEstablishmentId(UUID establishmentId) {
        return jpaRepository.findByEstablishmentIdOrderByStartDatetimeDesc(establishmentId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Booking> findByProfessionalAndDate(UUID professionalId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        return jpaRepository.findByProfessionalAndDate(professionalId, dayStart, dayEnd)
                .stream().map(this::toDomain).toList();
    }

    private Booking toDomain(BookingEntity e) {
        return Booking.builder()
                .id(e.getId())
                .clientId(e.getClientId())
                .professionalId(e.getProfessionalId())
                .establishmentId(e.getEstablishmentId())
                .providedServiceId(e.getProvidedServiceId())
                .startDatetime(e.getStartDatetime())
                .endDatetime(e.getEndDatetime())
                .status(e.getStatus())
                .price(e.getPrice())
                .durationMinutes(e.getDurationMinutes())
                .notes(e.getNotes())
                .cancelReason(e.getCancelReason())
                .createdAt(e.getCreatedAt())
                .cancelledAt(e.getCancelledAt())
                .build();
    }

    private BookingEntity toEntity(Booking b) {
        return BookingEntity.builder()
                .id(b.getId())
                .clientId(b.getClientId())
                .professionalId(b.getProfessionalId())
                .establishmentId(b.getEstablishmentId())
                .providedServiceId(b.getProvidedServiceId())
                .startDatetime(b.getStartDatetime())
                .endDatetime(b.getEndDatetime())
                .status(b.getStatus())
                .price(b.getPrice())
                .durationMinutes(b.getDurationMinutes())
                .notes(b.getNotes())
                .cancelReason(b.getCancelReason())
                .createdAt(b.getCreatedAt())
                .cancelledAt(b.getCancelledAt())
                .build();
    }
}
