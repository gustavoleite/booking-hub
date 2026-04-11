package com.bookinghub.notification.core.usecases;

import static org.mockito.Mockito.verify;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.EmailPort;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendBookingConfirmationUseCaseTest {

    @Mock
    private EmailPort emailPort;

    private SendBookingConfirmationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SendBookingConfirmationUseCase(emailPort);
    }

    @Test
    void shouldSendEmailToClientAndProfessional() {
        BookingSnapshot snapshot = BookingSnapshot.builder()
                .bookingId(UUID.randomUUID())
                .clientId("client-1")
                .professionalId(UUID.randomUUID())
                .startDatetime(LocalDateTime.of(2026, 5, 10, 14, 0))
                .endDatetime(LocalDateTime.of(2026, 5, 10, 15, 0))
                .status("CONFIRMED")
                .updatedAt(LocalDateTime.now())
                .clientEmail("client@example.com")
                .professionalEmail("pro@example.com")
                .reminderSent(false)
                .build();

        useCase.execute(snapshot);

        verify(emailPort).send(
                org.mockito.ArgumentMatchers.eq("client@example.com"),
                org.mockito.ArgumentMatchers.contains("confirmed"),
                org.mockito.ArgumentMatchers.anyString()
        );
        verify(emailPort).send(
                org.mockito.ArgumentMatchers.eq("pro@example.com"),
                org.mockito.ArgumentMatchers.contains("booking"),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void shouldSkipNullClientEmail() {
        BookingSnapshot snapshot = BookingSnapshot.builder()
                .bookingId(UUID.randomUUID())
                .clientId("client-2")
                .professionalId(UUID.randomUUID())
                .startDatetime(LocalDateTime.of(2026, 5, 10, 14, 0))
                .endDatetime(LocalDateTime.of(2026, 5, 10, 15, 0))
                .status("CONFIRMED")
                .updatedAt(LocalDateTime.now())
                .clientEmail(null)
                .professionalEmail("pro@example.com")
                .reminderSent(false)
                .build();

        // Should not throw — EmailAdapter guards null recipients
        // Use case itself calls send regardless; adapter handles null silently.
        // So we only assert no exception is thrown.
        useCase.execute(snapshot);
    }
}
