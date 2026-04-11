package com.bookinghub.notification.bdd;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import com.bookinghub.notification.core.usecases.HandleBookingCancelledUseCase;
import com.bookinghub.notification.core.usecases.HandleBookingCompletedUseCase;
import com.bookinghub.notification.core.usecases.HandleBookingCreatedUseCase;
import com.bookinghub.notification.core.usecases.SendBookingReminderUseCase;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDateTime;
import java.util.UUID;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailNotificationSteps {

    @Autowired
    private HandleBookingCreatedUseCase handleBookingCreatedUseCase;

    @Autowired
    private HandleBookingCancelledUseCase handleBookingCancelledUseCase;

    @Autowired
    private HandleBookingCompletedUseCase handleBookingCompletedUseCase;

    @Autowired
    private SendBookingReminderUseCase sendBookingReminderUseCase;

    @Autowired
    private BookingSnapshotRepository snapshotRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    private UUID currentBookingId;
    private String currentClientEmail;
    private String currentProfessionalEmail;

    @Given("a booking event is created for client {string} and professional {string} at {string}")
    public void aBookingEventIsCreated(String clientEmail, String professionalEmail, String start) {
        Mockito.reset(javaMailSender);
        currentBookingId = UUID.randomUUID();
        currentClientEmail = clientEmail;
        currentProfessionalEmail = professionalEmail;
        LocalDateTime startDt = LocalDateTime.parse(start);
        LocalDateTime endDt = startDt.plusHours(1);

        handleBookingCreatedUseCase.execute(
                currentBookingId,
                "client-bdd-email",
                UUID.randomUUID(),
                startDt,
                endDt,
                clientEmail,
                professionalEmail
        );
    }

    @When("the booking created event is processed")
    public void theBookingCreatedEventIsProcessed() {
        // execution already happened in @Given; step is a marker for readability
    }

    @Then("a confirmation email should be sent to {string}")
    public void aConfirmationEmailShouldBeSentTo(String email) {
        verify(javaMailSender, atLeastOnce()).send(argThat((SimpleMailMessage msg) ->
                msg.getTo() != null
                        && msg.getTo().length > 0
                        && email.equals(msg.getTo()[0])
        ));
    }

    @Given("an existing confirmed booking snapshot for client {string} and professional {string}")
    public void anExistingConfirmedBookingSnapshot(String clientEmail, String professionalEmail) {
        Mockito.reset(javaMailSender);
        currentBookingId = UUID.randomUUID();
        currentClientEmail = clientEmail;
        currentProfessionalEmail = professionalEmail;

        BookingSnapshot snapshot = BookingSnapshot.builder()
                .bookingId(currentBookingId)
                .clientId("client-bdd-email2")
                .professionalId(UUID.randomUUID())
                .startDatetime(LocalDateTime.now().plusDays(2))
                .endDatetime(LocalDateTime.now().plusDays(2).plusHours(1))
                .status("CONFIRMED")
                .updatedAt(LocalDateTime.now())
                .clientEmail(clientEmail)
                .professionalEmail(professionalEmail)
                .reminderSent(false)
                .build();

        snapshotRepository.save(snapshot);
    }

    @When("the booking cancelled event is processed")
    public void theBookingCancelledEventIsProcessed() {
        handleBookingCancelledUseCase.execute(currentBookingId);
    }

    @Then("a cancellation email should be sent to {string}")
    public void aCancellationEmailShouldBeSentTo(String email) {
        verify(javaMailSender, atLeastOnce()).send(argThat((SimpleMailMessage msg) ->
                msg.getTo() != null
                        && msg.getTo().length > 0
                        && email.equals(msg.getTo()[0])
        ));
    }

    @When("the booking completed event is processed")
    public void theBookingCompletedEventIsProcessed() {
        handleBookingCompletedUseCase.execute(currentBookingId);
    }

    @Then("a completed email should be sent to {string}")
    public void aCompletedEmailShouldBeSentTo(String email) {
        verify(javaMailSender, atLeastOnce()).send(argThat((SimpleMailMessage msg) ->
                msg.getTo() != null
                        && msg.getTo().length > 0
                        && email.equals(msg.getTo()[0])
        ));
    }

    @Given("there are no bookings due in the next 24 hours")
    public void thereAreNoBookingsDueInTheNext24Hours() {
        Mockito.reset(javaMailSender);
        // H2 is empty at this point (or no CONFIRMED bookings in the 23-25h window)
    }

    @When("the reminder job runs")
    public void theReminderJobRuns() {
        sendBookingReminderUseCase.execute();
    }

    @Then("no reminder emails should be sent")
    public void noReminderEmailsShouldBeSent() {
        verify(javaMailSender, never()).send(org.mockito.ArgumentMatchers.<SimpleMailMessage>any());
    }
}
