package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.domain.DaySchedule;
import com.bookinghub.booking.core.domain.ScheduleInfo;
import com.bookinghub.booking.core.exceptions.SlotUnavailableException;
import com.bookinghub.booking.core.ports.BookingEventPublisher;
import com.bookinghub.booking.core.ports.BookingRepository;
import com.bookinghub.booking.core.ports.CatalogServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateBookingUseCaseTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CatalogServiceClient catalogServiceClient;
    @Mock
    private BookingEventPublisher eventPublisher;

    @InjectMocks
    private CreateBookingUseCase useCase;

    private UUID professionalId;
    private UUID establishmentId;
    private UUID serviceId;
    private ScheduleInfo scheduleInfo;

    @BeforeEach
    void setUp() {
        professionalId = UUID.randomUUID();
        establishmentId = UUID.randomUUID();
        serviceId = UUID.randomUUID();

        // Monday (dayOfWeek=1), 09:00-18:00
        DaySchedule monday = new DaySchedule(1, LocalTime.of(9, 0), LocalTime.of(18, 0));
        scheduleInfo = new ScheduleInfo(true, new BigDecimal("50.00"), 60, List.of(monday));
    }

    @Test
    void shouldCreateBookingSuccessfully() {
        // Next Monday at 10:00
        LocalDateTime nextMonday = nextMonday().withHour(10).withMinute(0).withSecond(0).withNano(0);
        when(catalogServiceClient.getSchedule(establishmentId, professionalId, serviceId)).thenReturn(scheduleInfo);
        when(bookingRepository.existsActiveSlot(professionalId, nextMonday)).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = useCase.execute("user1", professionalId, establishmentId, serviceId, nextMonday, "test notes");

        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo("user1");
        assertThat(result.getPrice()).isEqualByComparingTo("50.00");
        assertThat(result.getDurationMinutes()).isEqualTo(60);
        verify(eventPublisher).publishBookingCreated(result);
    }

    @Test
    void shouldThrowWhenSlotIsAlreadyBooked() {
        LocalDateTime nextMonday = nextMonday().withHour(10).withMinute(0).withSecond(0).withNano(0);
        when(catalogServiceClient.getSchedule(establishmentId, professionalId, serviceId)).thenReturn(scheduleInfo);
        when(bookingRepository.existsActiveSlot(professionalId, nextMonday)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("user1", professionalId, establishmentId, serviceId, nextMonday, null))
                .isInstanceOf(SlotUnavailableException.class)
                .hasMessageContaining("already booked");

        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publishBookingCreated(any());
    }

    @Test
    void shouldThrowWhenDateIsInThePast() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);

        assertThatThrownBy(() -> useCase.execute("user1", professionalId, establishmentId, serviceId, past, null))
                .isInstanceOf(SlotUnavailableException.class)
                .hasMessageContaining("future");

        verifyNoInteractions(catalogServiceClient, bookingRepository, eventPublisher);
    }

    @Test
    void shouldThrowWhenProfessionalDoesNotWorkOnRequestedDay() {
        // ScheduleInfo only has Monday — use a Sunday
        LocalDateTime sunday = nextSunday().withHour(10).withMinute(0).withSecond(0).withNano(0);
        when(catalogServiceClient.getSchedule(establishmentId, professionalId, serviceId)).thenReturn(scheduleInfo);

        assertThatThrownBy(() -> useCase.execute("user1", professionalId, establishmentId, serviceId, sunday, null))
                .isInstanceOf(SlotUnavailableException.class)
                .hasMessageContaining("does not work on day");
    }

    @Test
    void shouldThrowWhenTimeIsOutsideWorkingHours() {
        LocalDateTime nextMonday = nextMonday().withHour(20).withMinute(0).withSecond(0).withNano(0);
        when(catalogServiceClient.getSchedule(establishmentId, professionalId, serviceId)).thenReturn(scheduleInfo);

        assertThatThrownBy(() -> useCase.execute("user1", professionalId, establishmentId, serviceId, nextMonday, null))
                .isInstanceOf(SlotUnavailableException.class)
                .hasMessageContaining("outside the professional's working hours");
    }

    private LocalDateTime nextMonday() {
        LocalDateTime now = LocalDateTime.now().plusDays(1);
        while (now.getDayOfWeek().getValue() != 1) {
            now = now.plusDays(1);
        }
        return now;
    }

    private LocalDateTime nextSunday() {
        LocalDateTime now = LocalDateTime.now().plusDays(1);
        while (now.getDayOfWeek().getValue() != 7) {
            now = now.plusDays(1);
        }
        return now;
    }
}
