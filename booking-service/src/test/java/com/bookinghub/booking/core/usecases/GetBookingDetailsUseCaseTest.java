package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.domain.BookingStatus;
import com.bookinghub.booking.core.exceptions.BookingNotFoundException;
import com.bookinghub.booking.core.exceptions.ForbiddenBookingAccessException;
import com.bookinghub.booking.core.ports.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBookingDetailsUseCaseTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private GetBookingDetailsUseCase useCase;

    private Booking buildBooking(String clientId) {
        return Booking.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .professionalId(UUID.randomUUID())
                .establishmentId(UUID.randomUUID())
                .providedServiceId(UUID.randomUUID())
                .startDatetime(LocalDateTime.now().plusDays(1))
                .endDatetime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(BookingStatus.CONFIRMED)
                .price(new BigDecimal("50.00"))
                .durationMinutes(60)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldGetDetailsAsClientOwner() {
        Booking booking = buildBooking("client1");
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Booking result = useCase.execute(booking.getId(), "client1", "ROLE_CLIENT");

        assertThat(result).isEqualTo(booking);
    }

    @Test
    void shouldGetDetailsAsProfessional() {
        Booking booking = buildBooking("client1");
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Booking result = useCase.execute(booking.getId(), "professional1", "ROLE_PROFESSIONAL");

        assertThat(result).isEqualTo(booking);
    }

    @Test
    void shouldGetDetailsAsOwner() {
        Booking booking = buildBooking("client1");
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Booking result = useCase.execute(booking.getId(), "owner1", "ROLE_OWNER");

        assertThat(result).isEqualTo(booking);
    }

    @Test
    void shouldThrowForbiddenWhenClientIsNotOwner() {
        Booking booking = buildBooking("client1");
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> useCase.execute(booking.getId(), "other-client", "ROLE_CLIENT"))
                .isInstanceOf(ForbiddenBookingAccessException.class);
    }

    @Test
    void shouldThrowNotFoundWhenBookingDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(bookingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id, "user1", "ROLE_CLIENT"))
                .isInstanceOf(BookingNotFoundException.class);
    }
}
