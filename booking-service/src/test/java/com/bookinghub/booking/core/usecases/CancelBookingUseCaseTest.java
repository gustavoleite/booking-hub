package com.bookinghub.booking.core.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.domain.BookingStatus;
import com.bookinghub.booking.core.exceptions.BookingNotFoundException;
import com.bookinghub.booking.core.exceptions.ForbiddenBookingAccessException;
import com.bookinghub.booking.core.ports.BookingEventPublisher;
import com.bookinghub.booking.core.ports.BookingRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelBookingUseCaseTest {

  @Mock
  private BookingRepository bookingRepository;
  @Mock
  private BookingEventPublisher eventPublisher;

  @InjectMocks
  private CancelBookingUseCase useCase;

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
  void shouldCancelBookingAsClient() {
    Booking booking = buildBooking("client1");
    when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
    when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Booking result = useCase.execute(booking.getId(), "client1", "ROLE_CLIENT", "Changed mind");

    assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    assertThat(result.getCancelReason()).isEqualTo("Changed mind");
    verify(eventPublisher).publishBookingCancelled(result);
  }

  @Test
  void shouldCancelBookingAsOwner() {
    Booking booking = buildBooking("client1");
    when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
    when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Booking result = useCase.execute(booking.getId(), "owner1", "ROLE_OWNER", "No show expected");

    assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    verify(eventPublisher).publishBookingCancelled(result);
  }

  @Test
  void shouldThrowWhenClientTriesToCancelOtherClientBooking() {
    Booking booking = buildBooking("client1");
    when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

    assertThatThrownBy(() -> useCase.execute(booking.getId(), "other-client", "ROLE_CLIENT", null))
        .isInstanceOf(ForbiddenBookingAccessException.class);

    verify(bookingRepository, never()).save(any());
  }

  @Test
  void shouldThrowWhenBookingNotFound() {
    UUID id = UUID.randomUUID();
    when(bookingRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(id, "client1", "ROLE_CLIENT", null))
        .isInstanceOf(BookingNotFoundException.class);
  }
}
