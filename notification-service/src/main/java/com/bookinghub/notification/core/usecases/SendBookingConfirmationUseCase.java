package com.bookinghub.notification.core.usecases;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.EmailPort;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SendBookingConfirmationUseCase {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final EmailPort emailPort;

    public void execute(BookingSnapshot snapshot) {
        String when = snapshot.getStartDatetime().format(FMT);

        emailPort.send(
                snapshot.getClientEmail(),
                "Booking confirmed — " + when,
                "Hello!\n\nYour booking on " + when + " has been confirmed.\n\nSee you soon!"
        );

        emailPort.send(
                snapshot.getProfessionalEmail(),
                "New booking — " + when,
                "Hello!\n\nA new booking has been scheduled for " + when + ".\n\nCheck your agenda."
        );
    }
}
