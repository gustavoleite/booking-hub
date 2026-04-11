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
                "Como foi sua experiência? — " + when,
                "Olá!\n\nObrigado pela sua visita no dia " + when + "!"
                        + "\n\nEsperamos que tenha tido uma ótima experiência."
                        + " Sua opinião é muito importante para nós!"
        );
    }
}
