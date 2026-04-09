package com.bookinghub.notification.core.usecases;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import java.time.LocalDateTime;
import java.util.UUID;

public class HandleBookingCreatedUseCase {

  private final BookingSnapshotRepository repository;

  public HandleBookingCreatedUseCase(BookingSnapshotRepository repository) {
    this.repository = repository;
  }

  public void execute(
      UUID bookingId,
      String clientId,
      UUID professionalId,
      LocalDateTime startDatetime,
      LocalDateTime endDatetime) {

    BookingSnapshot snapshot = BookingSnapshot.builder()
        .bookingId(bookingId)
        .clientId(clientId)
        .professionalId(professionalId)
        .startDatetime(startDatetime)
        .endDatetime(endDatetime)
        .status("CONFIRMED")
        .updatedAt(LocalDateTime.now())
        .build();

    repository.save(snapshot);
  }
}
