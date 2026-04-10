package com.bookinghub.notification.core.usecases;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import com.bookinghub.notification.core.ports.EmailPort;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SendBookingReminderUseCase {

  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final BookingSnapshotRepository repository;
  private final EmailPort emailPort;

  public void execute() {
    LocalDateTime from = LocalDateTime.now().plusHours(23);
    LocalDateTime to = LocalDateTime.now().plusHours(25);

    List<BookingSnapshot> pending = repository.findConfirmedWithReminderPending(from, to);
    log.info("Reminder job: {} bookings to remind", pending.size());

    for (BookingSnapshot snapshot : pending) {
      String when = snapshot.getStartDatetime().format(FMT);

      emailPort.send(
          snapshot.getClientEmail(),
          "Reminder: your booking tomorrow at " + when,
          "Hello!\n\nThis is a reminder that you have a booking tomorrow at " + when + "."
      );

      emailPort.send(
          snapshot.getProfessionalEmail(),
          "Reminder: booking tomorrow at " + when,
          "Hello!\n\nReminder: you have a booking scheduled for tomorrow at " + when + "."
      );

      snapshot.markReminderSent();
      repository.save(snapshot);
    }
  }
}
