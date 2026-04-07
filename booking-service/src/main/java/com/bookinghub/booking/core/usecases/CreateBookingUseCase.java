package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.domain.DaySchedule;
import com.bookinghub.booking.core.domain.ScheduleInfo;
import com.bookinghub.booking.core.exceptions.CatalogServiceException;
import com.bookinghub.booking.core.exceptions.SlotUnavailableException;
import com.bookinghub.booking.core.ports.BookingEventPublisher;
import com.bookinghub.booking.core.ports.BookingRepository;
import com.bookinghub.booking.core.ports.CatalogServiceClient;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateBookingUseCase {

    private final BookingRepository bookingRepository;
    private final CatalogServiceClient catalogServiceClient;
    private final BookingEventPublisher eventPublisher;

    public Booking execute(String clientId, UUID professionalId, UUID establishmentId,
                         UUID serviceId, LocalDateTime startDatetime, String notes) {

        if (!startDatetime.isAfter(LocalDateTime.now())) {
            throw new SlotUnavailableException(
                    "Booking must be scheduled for a future date and time");
        }

        ScheduleInfo schedule = catalogServiceClient
                .getSchedule(establishmentId, professionalId, serviceId);

        if (!schedule.active()) {
            throw new CatalogServiceException("Professional is not active in this establishment");
        }

        int dayOfWeek = startDatetime.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
        DaySchedule daySchedule = schedule.workSchedule().stream()
                .filter(d -> d.dayOfWeek() == dayOfWeek)
                .findFirst()
                .orElseThrow(() -> new SlotUnavailableException(
                        "Professional does not work on day " + startDatetime.getDayOfWeek()));

        if (!daySchedule.contains(startDatetime.toLocalTime(), schedule.durationMinutes())) {
            throw new SlotUnavailableException(
                    "Requested time is outside the professional's working hours for this day");
        }

        if (bookingRepository.existsActiveSlot(professionalId, startDatetime)) {
            throw new SlotUnavailableException("This slot is already booked");
        }

        Booking booking = Booking.create(clientId, professionalId, establishmentId,
                serviceId, startDatetime, schedule.price(), schedule.durationMinutes(), notes);

        Booking saved = bookingRepository.save(booking);
        eventPublisher.publishBookingCreated(saved);
        return saved;
    }
}
