package com.bookinghub.booking.core.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.ports.BookingRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListClientBookingsUseCaseTest {

  @Mock
  private BookingRepository bookingRepository;

  @InjectMocks
  private ListClientBookingsUseCase useCase;

  @Test
  void shouldListBookingsByClient() {
    String clientId = "client1";
    List<Booking> bookings = List.of(Booking.builder().clientId(clientId).build());
    when(bookingRepository.findByClientId(clientId)).thenReturn(bookings);

    List<Booking> result = useCase.execute(clientId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getClientId()).isEqualTo(clientId);
  }
}
