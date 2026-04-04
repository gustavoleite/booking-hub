package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.ports.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListEstablishmentBookingsUseCaseTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ListEstablishmentBookingsUseCase useCase;

    @Test
    void shouldListBookingsByEstablishment() {
        UUID establishmentId = UUID.randomUUID();
        List<Booking> bookings = List.of(Booking.builder().establishmentId(establishmentId).build());
        when(bookingRepository.findByEstablishmentId(establishmentId)).thenReturn(bookings);

        List<Booking> result = useCase.execute(establishmentId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstablishmentId()).isEqualTo(establishmentId);
    }
}
