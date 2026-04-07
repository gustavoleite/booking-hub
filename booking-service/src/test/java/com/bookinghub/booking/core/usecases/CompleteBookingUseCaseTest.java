package com.bookinghub.booking.core.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
class CompleteBookingUseCaseTest {

  @Mock
  private BookingRepository bookingRepository;
  @Mock
  private BookingEventPublisher eventPublisher;
  @Mock
  private ConsumeBookingCompletedUseCase consumeBookingCompletedUseCase;

  @InjectMocks
  private CompleteBookingUseCase useCase;

  private Booking buildBooking() {
    return Booking.builder()
        .id(UUID.randomUUID())
        .clientId("client1")
        .professionalId(UUID.randomUUID())
        .establishmentId(UUID.randomUUID())
        .providedServiceId(UUID.randomUUID())
        .startDatetime(LocalDateTime.now().minusHours(1))
        .endDatetime(LocalDateTime.now())
        .status(BookingStatus.CONFIRMED)
        .price(new BigDecimal("50.00"))
        .durationMinutes(60)
        .createdAt(LocalDateTime.now().minusDays(1))
        .build();
  }

  @Test
  void shouldCompleteBookingAsProfessional() {
    Booking booking = buildBooking();
    when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
    when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Booking result = useCase.execute(booking.getId(), "ROLE_PROFESSIONAL");

    assertThat(result.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    verify(eventPublisher).publishBookingCompleted(result);
    verify(consumeBookingCompletedUseCase).execute(eq(result.getId()), eq(result.getClientId()),
        eq(result.getProfessionalId()), eq(result.getEstablishmentId()), any());
  }

  @Test
  void shouldCompleteBookingAsOwner() {
    Booking booking = buildBooking();
    when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
    when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Booking result = useCase.execute(booking.getId(), "ROLE_OWNER");

    assertThat(result.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    verify(eventPublisher).publishBookingCompleted(result);
    verify(consumeBookingCompletedUseCase).execute(any(), any(), any(), any(), any());
  }

  @Test
  void shouldThrowForbiddenWhenRoleIsNotAllowed() {
    UUID id = UUID.randomUUID();
    assertThatThrownBy(() -> useCase.execute(id, "ROLE_CLIENT"))
        .isInstanceOf(ForbiddenBookingAccessException.class);

    verify(bookingRepository, never()).findById(any());
  }

  @Test
  void shouldThrowNotFoundWhenBookingDoesNotExist() {
    UUID id = UUID.randomUUID();
    when(bookingRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(id, "ROLE_PROFESSIONAL"))
        .isInstanceOf(BookingNotFoundException.class);
  }
}
