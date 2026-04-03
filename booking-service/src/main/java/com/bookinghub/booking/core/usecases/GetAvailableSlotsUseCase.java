package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.domain.DaySchedule;
import com.bookinghub.booking.core.domain.ScheduleInfo;
import com.bookinghub.booking.core.ports.BookingRepository;
import com.bookinghub.booking.core.ports.CatalogServiceClient;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetAvailableSlotsUseCase {

    private final BookingRepository bookingRepository;
    private final CatalogServiceClient catalogServiceClient;

    public record Result(int durationMinutes, BigDecimal price, List<LocalDateTime> availableSlots) {}

    public Result execute(UUID establishmentId, UUID professionalId, UUID serviceId, LocalDate date) {

        ScheduleInfo schedule = catalogServiceClient.getSchedule(establishmentId, professionalId, serviceId);

        int dayOfWeek = date.getDayOfWeek().getValue();
        DaySchedule daySchedule = schedule.workSchedule().stream()
                .filter(d -> d.dayOfWeek() == dayOfWeek)
                .findFirst()
                .orElse(null);

        if (daySchedule == null || !schedule.active()) {
            return new Result(schedule.durationMinutes(), schedule.price(), List.of());
        }

        Set<LocalDateTime> takenSlots = bookingRepository.findByProfessionalAndDate(professionalId, date)
                .stream()
                .map(Booking::getStartDatetime)
                .collect(Collectors.toSet());

        List<LocalDateTime> available = new ArrayList<>();
        LocalTime cursor = daySchedule.startTime();
        int duration = schedule.durationMinutes();

        while (!cursor.plusMinutes(duration).isAfter(daySchedule.endTime())) {
            LocalDateTime slot = date.atTime(cursor);
            if (!takenSlots.contains(slot) && slot.isAfter(LocalDateTime.now())) {
                available.add(slot);
            }
            cursor = cursor.plusMinutes(duration);
        }

        return new Result(duration, schedule.price(), available);
    }
}
