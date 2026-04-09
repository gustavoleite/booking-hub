package com.bookinghub.notification.core.usecases;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import java.util.Optional;
import java.util.UUID;

public class HandleBookingCompletedUseCase {

  private final BookingSnapshotRepository repository;

  public HandleBookingCompletedUseCase(BookingSnapshotRepository repository) {
    this.repository = repository;
  }

  public void execute(UUID bookingId) {
    Optional<BookingSnapshot> existing = repository.findByBookingId(bookingId);
    if (existing.isPresent()) {
      existing.get().updateStatus("COMPLETED");
      repository.save(existing.get());
    }
  }
}
