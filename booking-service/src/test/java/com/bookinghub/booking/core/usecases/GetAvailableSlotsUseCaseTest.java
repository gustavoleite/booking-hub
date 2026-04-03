package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.DaySchedule;
import com.bookinghub.booking.core.domain.ScheduleInfo;
import com.bookinghub.booking.core.ports.BookingRepository;
import com.bookinghub.booking.core.ports.CatalogServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAvailableSlotsUseCaseTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CatalogServiceClient catalogServiceClient;

    @InjectMocks
    private GetAvailableSlotsUseCase useCase;

    @Test
    void shouldReturnEmptyWhenProfessionalDoesNotWorkOnDay() {
        UUID estId = UUID.randomUUID();
        UUID profId = UUID.randomUUID();
        UUID svcId = UUID.randomUUID();

        // Only Monday schedule, test with Sunday
        DaySchedule monday = new DaySchedule(1, LocalTime.of(9, 0), LocalTime.of(18, 0));
        ScheduleInfo schedule = new ScheduleInfo(true, new BigDecimal("50.00"), 60, List.of(monday));
        when(catalogServiceClient.getSchedule(estId, profId, svcId)).thenReturn(schedule);

        // Find next Sunday
        LocalDate sunday = LocalDate.now().plusDays(1);
        while (sunday.getDayOfWeek().getValue() != 7) {
            sunday = sunday.plusDays(1);
        }

        GetAvailableSlotsUseCase.Result result = useCase.execute(estId, profId, svcId, sunday);

        assertThat(result.availableSlots()).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenScheduleIsInactive() {
        UUID estId = UUID.randomUUID();
        UUID profId = UUID.randomUUID();
        UUID svcId = UUID.randomUUID();

        DaySchedule monday = new DaySchedule(1, LocalTime.of(9, 0), LocalTime.of(18, 0));
        ScheduleInfo schedule = new ScheduleInfo(false, new BigDecimal("50.00"), 60, List.of(monday));
        when(catalogServiceClient.getSchedule(estId, profId, svcId)).thenReturn(schedule);

        LocalDate nextMonday = nextMonday();
        GetAvailableSlotsUseCase.Result result = useCase.execute(estId, profId, svcId, nextMonday);

        assertThat(result.availableSlots()).isEmpty();
    }

    @Test
    void shouldReturnAvailableSlotsForFutureDate() {
        UUID estId = UUID.randomUUID();
        UUID profId = UUID.randomUUID();
        UUID svcId = UUID.randomUUID();

        LocalDate nextMonday = nextMonday();
        // Monday = dayOfWeek 1
        int dayOfWeek = nextMonday.getDayOfWeek().getValue();
        DaySchedule day = new DaySchedule(dayOfWeek, LocalTime.of(9, 0), LocalTime.of(11, 0));
        ScheduleInfo schedule = new ScheduleInfo(true, new BigDecimal("80.00"), 60, List.of(day));
        when(catalogServiceClient.getSchedule(estId, profId, svcId)).thenReturn(schedule);
        when(bookingRepository.findByProfessionalAndDate(profId, nextMonday)).thenReturn(List.of());

        GetAvailableSlotsUseCase.Result result = useCase.execute(estId, profId, svcId, nextMonday);

        // 09:00 and 10:00 should be available (2 slots of 60min from 9-11)
        assertThat(result.availableSlots()).hasSize(2);
        assertThat(result.durationMinutes()).isEqualTo(60);
        assertThat(result.price()).isEqualByComparingTo("80.00");
    }

    private LocalDate nextMonday() {
        LocalDate d = LocalDate.now().plusDays(1);
        while (d.getDayOfWeek().getValue() != 1) {
            d = d.plusDays(1);
        }
        return d;
    }
}
