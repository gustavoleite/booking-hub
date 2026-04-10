package com.bookinghub.notification.core.usecases;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import java.util.Optional;
import java.util.UUID;

public class HandleBookingCancelledUseCase {

  private final BookingSnapshotRepository repository;
  private final SendBookingCancellationUseCase sendCancellation;

  public HandleBookingCancelledUseCase(BookingSnapshotRepository repository,
      SendBookingCancellationUseCase sendCancellation) {
    this.repository = repository;
    this.sendCancellation = sendCancellation;
  }

  public void execute(UUID bookingId) {
    Optional<BookingSnapshot> existing = repository.findByBookingId(bookingId);
    if (existing.isPresent()) {
      BookingSnapshot snapshot = existing.get();
      snapshot.updateStatus("CANCELLED");
      repository.save(snapshot);
      sendCancellation.execute(snapshot);
    }
  }
}
