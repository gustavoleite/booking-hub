package com.bookinghub.booking.core.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.ports.BookingRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListProfessionalAgendaUseCaseTest {

  @Mock
  private BookingRepository bookingRepository;

  @InjectMocks
  private ListProfessionalAgendaUseCase useCase;

  @Test
  void shouldListBookingsByProfessional() {
    UUID professionalId = UUID.randomUUID();
    List<Booking> bookings = List.of(Booking.builder().professionalId(professionalId).build());
    when(bookingRepository.findByProfessionalId(professionalId)).thenReturn(bookings);

    List<Booking> result = useCase.execute(professionalId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getProfessionalId()).isEqualTo(professionalId);
  }
}
