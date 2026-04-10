package com.bookinghub.notification.core.usecases;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import java.time.LocalDateTime;
import java.util.UUID;

public class HandleBookingCreatedUseCase {

  private final BookingSnapshotRepository repository;
  private final SendBookingConfirmationUseCase sendConfirmation;

  public HandleBookingCreatedUseCase(BookingSnapshotRepository repository,
      SendBookingConfirmationUseCase sendConfirmation) {
    this.repository = repository;
    this.sendConfirmation = sendConfirmation;
  }

  public void execute(
      UUID bookingId,
      String clientId,
      UUID professionalId,
      LocalDateTime startDatetime,
      LocalDateTime endDatetime,
      String clientEmail,
      String professionalEmail) {

    BookingSnapshot snapshot = BookingSnapshot.builder()
        .bookingId(bookingId)
        .clientId(clientId)
        .professionalId(professionalId)
        .startDatetime(startDatetime)
        .endDatetime(endDatetime)
        .status("CONFIRMED")
        .updatedAt(LocalDateTime.now())
        .clientEmail(clientEmail)
        .professionalEmail(professionalEmail)
        .reminderSent(false)
        .build();

    repository.save(snapshot);
    sendConfirmation.execute(snapshot);
  }
}
