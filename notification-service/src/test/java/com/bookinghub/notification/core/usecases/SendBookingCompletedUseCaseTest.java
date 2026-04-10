package com.bookinghub.notification.core.usecases;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
class SendBookingCompletedUseCaseTest {

    @Mock
    private EmailPort emailPort;

    private SendBookingCompletedUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SendBookingCompletedUseCase(emailPort);
    }

    @Test
    void shouldSendCompletedEmailOnlyToClient() {
        BookingSnapshot snapshot = BookingSnapshot.builder()
                .bookingId(UUID.randomUUID())
                .clientId("client-1")
                .professionalId(UUID.randomUUID())
                .startDatetime(LocalDateTime.of(2026, 7, 20, 10, 0))
                .endDatetime(LocalDateTime.of(2026, 7, 20, 11, 0))
                .status("COMPLETED")
                .updatedAt(LocalDateTime.now())
                .clientEmail("client@example.com")
                .professionalEmail("pro@example.com")
                .reminderSent(false)
                .build();

        useCase.execute(snapshot);

        // Only the client receives the "rate your experience" email
        verify(emailPort).send(
                eq("client@example.com"),
                anyString(),
                anyString()
        );
        verifyNoMoreInteractions(emailPort);
    }

    @Test
    void shouldIncludeReviewPromptInEmailBody() {
        BookingSnapshot snapshot = BookingSnapshot.builder()
                .bookingId(UUID.randomUUID())
                .clientId("client-1")
                .professionalId(UUID.randomUUID())
                .startDatetime(LocalDateTime.of(2026, 7, 20, 10, 0))
                .endDatetime(LocalDateTime.of(2026, 7, 20, 11, 0))
                .status("COMPLETED")
                .updatedAt(LocalDateTime.now())
                .clientEmail("client@example.com")
                .professionalEmail("pro@example.com")
                .reminderSent(false)
                .build();

        useCase.execute(snapshot);

        verify(emailPort).send(
                eq("client@example.com"),
                anyString(),
                contains("review")
        );
    }
}
