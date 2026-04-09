package com.bookinghub.notification.core.usecases;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HandleBookingCreatedUseCaseTest {

  @Mock
  private BookingSnapshotRepository repository;

  private HandleBookingCreatedUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new HandleBookingCreatedUseCase(repository);
  }

  @Test
  void shouldSaveSnapshotWithConfirmedStatus() {
    UUID bookingId = UUID.randomUUID();
    UUID professionalId = UUID.randomUUID();
    LocalDateTime start = LocalDateTime.now().plusDays(1);
    LocalDateTime end = start.plusHours(1);

    useCase.execute(bookingId, "client-123", professionalId, start, end);

    verify(repository).save(argThat(snapshot ->
        snapshot.getBookingId().equals(bookingId)
            && snapshot.getClientId().equals("client-123")
            && snapshot.getProfessionalId().equals(professionalId)
            && snapshot.getStartDatetime().equals(start)
            && snapshot.getEndDatetime().equals(end)
            && "CONFIRMED".equals(snapshot.getStatus())
    ));
  }
}
