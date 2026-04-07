package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.EligibleBooking;
import com.bookinghub.booking.core.ports.EligibleBookingRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ConsumeBookingCompletedUseCase {

    private final EligibleBookingRepository eligibleBookingRepository;

    public void execute(UUID bookingId, String clientId, UUID professionalId,
                      UUID establishmentId, LocalDateTime completedAt) {
        if (eligibleBookingRepository.existsById(bookingId)) {
            log.debug("Booking {} already registered as eligible — skipping (idempotent)",
                    bookingId);
            return;
        }
        EligibleBooking eligible = new EligibleBooking(
                bookingId, clientId, professionalId, establishmentId, completedAt);
        eligibleBookingRepository.save(eligible);
        log.info("Registered booking {} as eligible for review", bookingId);
    }
}
