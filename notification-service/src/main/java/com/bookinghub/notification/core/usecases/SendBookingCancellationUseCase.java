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
                "Agendamento cancelado — " + when,
                "Olá!\n\nSeu agendamento do dia " + when + " foi cancelado."
                        + "\n\nSe precisar remarcar, estamos à disposição!"
        );

        emailPort.send(
                snapshot.getProfessionalEmail(),
                "Agendamento cancelado — " + when,
                "Olá!\n\nO agendamento do dia " + when + " foi cancelado pelo cliente."
                        + "\n\nConsulte sua agenda para verificar sua disponibilidade."
        );
    }
}
