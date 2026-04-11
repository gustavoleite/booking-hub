package com.bookinghub.notification.core.usecases;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
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
class SendBookingCancellationUseCaseTest {

    @Mock
    private EmailPort emailPort;

    private SendBookingCancellationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SendBookingCancellationUseCase(emailPort);
    }

    @Test
    void shouldSendCancellationEmailToClientAndProfessional() {
        BookingSnapshot snapshot = BookingSnapshot.builder()
                .bookingId(UUID.randomUUID())
                .clientId("client-1")
                .professionalId(UUID.randomUUID())
                .startDatetime(LocalDateTime.of(2026, 6, 15, 9, 0))
                .endDatetime(LocalDateTime.of(2026, 6, 15, 10, 0))
                .status("CANCELLED")
                .updatedAt(LocalDateTime.now())
                .clientEmail("client@example.com")
                .professionalEmail("pro@example.com")
                .reminderSent(false)
                .build();

        useCase.execute(snapshot);

        verify(emailPort).send(
                eq("client@example.com"),
                contains("cancelado"),
                anyString()
        );
        verify(emailPort).send(
                eq("pro@example.com"),
                contains("cancelado"),
                anyString()
        );
    }

    @Test
    void shouldNotThrowWhenEmailsAreNull() {
        BookingSnapshot snapshot = BookingSnapshot.builder()
                .bookingId(UUID.randomUUID())
                .clientId("client-2")
                .professionalId(UUID.randomUUID())
                .startDatetime(LocalDateTime.of(2026, 6, 15, 9, 0))
                .endDatetime(LocalDateTime.of(2026, 6, 15, 10, 0))
                .status("CANCELLED")
                .updatedAt(LocalDateTime.now())
                .clientEmail(null)
                .professionalEmail(null)
                .reminderSent(false)
                .build();

        useCase.execute(snapshot);
        // No exception expected — EmailAdapter guards null recipients silently
    }
}
