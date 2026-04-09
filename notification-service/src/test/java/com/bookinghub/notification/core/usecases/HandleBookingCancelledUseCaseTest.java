package com.bookinghub.notification.core.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HandleBookingCancelledUseCaseTest {

  @Mock
  private BookingSnapshotRepository repository;

  private HandleBookingCancelledUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new HandleBookingCancelledUseCase(repository);
  }

  @Test
  void shouldUpdateStatusToCancelledWhenSnapshotExists() {
    UUID bookingId = UUID.randomUUID();
    BookingSnapshot snapshot = BookingSnapshot.builder()
        .bookingId(bookingId)
        .clientId("client-1")
        .professionalId(UUID.randomUUID())
        .startDatetime(LocalDateTime.now().plusDays(1))
        .endDatetime(LocalDateTime.now().plusDays(1).plusHours(1))
        .status("CONFIRMED")
        .updatedAt(LocalDateTime.now())
        .build();

    when(repository.findByBookingId(bookingId)).thenReturn(Optional.of(snapshot));

    useCase.execute(bookingId);

    assertThat(snapshot.getStatus()).isEqualTo("CANCELLED");
    verify(repository).save(argThat(s -> "CANCELLED".equals(s.getStatus())));
  }

  @Test
  void shouldDoNothingWhenSnapshotNotFound() {
    UUID bookingId = UUID.randomUUID();
    when(repository.findByBookingId(bookingId)).thenReturn(Optional.empty());

    useCase.execute(bookingId);

    verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
  }
}
