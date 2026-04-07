package com.bookinghub.booking.core.ports;

import com.bookinghub.booking.core.domain.Booking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(UUID id);
    boolean existsActiveSlot(UUID professionalId, LocalDateTime startDatetime);
    List<Booking> findByClientId(String clientId);
    List<Booking> findByProfessionalId(UUID professionalId);
    List<Booking> findByEstablishmentId(UUID establishmentId);
    List<Booking> findByProfessionalAndDate(UUID professionalId, LocalDate date);
}
