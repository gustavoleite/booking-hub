package com.bookinghub.notification.core.ports;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingSnapshotRepository {

  void save(BookingSnapshot snapshot);

  Optional<BookingSnapshot> findByBookingId(UUID bookingId);

  List<BookingSnapshot> findByClientId(String clientId);

  List<BookingSnapshot> findByProfessionalId(UUID professionalId);

  List<BookingSnapshot> findConfirmedWithReminderPending(LocalDateTime from, LocalDateTime to);
}
