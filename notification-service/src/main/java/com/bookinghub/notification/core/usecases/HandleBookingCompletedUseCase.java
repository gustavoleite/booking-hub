package com.bookinghub.notification.core.usecases;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import java.util.Optional;
import java.util.UUID;

public class HandleBookingCompletedUseCase {

  private final BookingSnapshotRepository repository;
  private final SendBookingCompletedUseCase sendCompleted;

  public HandleBookingCompletedUseCase(BookingSnapshotRepository repository,
      SendBookingCompletedUseCase sendCompleted) {
    this.repository = repository;
    this.sendCompleted = sendCompleted;
  }

  public void execute(UUID bookingId) {
    Optional<BookingSnapshot> existing = repository.findByBookingId(bookingId);
    if (existing.isPresent()) {
      BookingSnapshot snapshot = existing.get();
      snapshot.updateStatus("COMPLETED");
      repository.save(snapshot);
      sendCompleted.execute(snapshot);
    }
  }
}
