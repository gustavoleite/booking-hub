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
                "Agendamento confirmado — " + when,
                "Olá!\n\nSeu agendamento para o dia " + when + " foi confirmado com sucesso."
                        + "\n\nEstamos te esperando. Até breve!"
        );

        emailPort.send(
                snapshot.getProfessionalEmail(),
                "Novo agendamento — " + when,
                "Olá!\n\nVocê tem um novo agendamento marcado para o dia " + when + "."
                        + "\n\nConsulte sua agenda para mais detalhes."
        );
    }
}
