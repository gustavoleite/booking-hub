package com.bookinghub.notification.core.usecases;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.EmailPort;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SendBookingCancellationUseCase {

  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final EmailPort emailPort;

  public void execute(BookingSnapshot snapshot) {
    String when = snapshot.getStartDatetime().format(FMT);

    emailPort.send(
        snapshot.getClientEmail(),
        "Booking cancelled — " + when,
        "Hello!\n\nYour booking scheduled for " + when + " has been cancelled."
    );

    emailPort.send(
        snapshot.getProfessionalEmail(),
        "Booking cancelled — " + when,
        "Hello!\n\nThe booking scheduled for " + when + " has been cancelled by the client."
    );
  }
}
