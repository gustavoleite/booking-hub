package com.bookinghub.notification.core.usecases;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.EmailPort;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SendBookingCompletedUseCase {

  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final EmailPort emailPort;

  public void execute(BookingSnapshot snapshot) {
    String when = snapshot.getStartDatetime().format(FMT);

    emailPort.send(
        snapshot.getClientEmail(),
        "How was your experience? — " + when,
        "Hello!\n\nThank you for your visit on " + when
            + ".\n\nWe hope you had a great experience. Feel free to leave a review!"
    );
  }
}
