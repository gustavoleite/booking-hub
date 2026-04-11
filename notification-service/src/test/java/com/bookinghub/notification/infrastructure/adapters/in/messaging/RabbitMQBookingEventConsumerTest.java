package com.bookinghub.notification.infrastructure.adapters.in.messaging;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.bookinghub.notification.core.usecases.HandleBookingCancelledUseCase;
import com.bookinghub.notification.core.usecases.HandleBookingCompletedUseCase;
import com.bookinghub.notification.core.usecases.HandleBookingCreatedUseCase;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RabbitMQBookingEventConsumerTest {

    @Mock
    private HandleBookingCreatedUseCase handleCreated;

    @Mock
    private HandleBookingCancelledUseCase handleCancelled;

    @Mock
    private HandleBookingCompletedUseCase handleCompleted;

    private RabbitMQBookingEventConsumer consumer;

    private BookingEventPayload payload;

    @BeforeEach
    void setUp() {
        consumer = new RabbitMQBookingEventConsumer(handleCreated, handleCancelled, handleCompleted);

        payload = new BookingEventPayload(
                UUID.randomUUID(), "client-1", UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                BigDecimal.TEN, 60, "CONFIRMED", LocalDateTime.now(),
                "client@example.com", "pro@example.com");
    }

    @Test
    void shouldDelegateToHandleCreated_whenRoutingKeyIsBookingCreated() {
        consumer.handle(payload, "booking.created");

        verify(handleCreated).execute(
                payload.bookingId(), payload.clientId(), payload.professionalId(),
                payload.startDatetime(), payload.endDatetime(),
                payload.clientEmail(), payload.professionalEmail());
        verifyNoInteractions(handleCancelled, handleCompleted);
    }

    @Test
    void shouldDelegateToHandleCancelled_whenRoutingKeyIsBookingCancelled() {
        consumer.handle(payload, "booking.cancelled");

        verify(handleCancelled).execute(payload.bookingId());
        verifyNoInteractions(handleCreated, handleCompleted);
    }

    @Test
    void shouldDelegateToHandleCompleted_whenRoutingKeyIsBookingCompleted() {
        consumer.handle(payload, "booking.completed");

        verify(handleCompleted).execute(payload.bookingId());
        verifyNoInteractions(handleCreated, handleCancelled);
    }

    @Test
    void shouldIgnoreUnknownRoutingKey_withoutThrowingException() {
        consumer.handle(payload, "booking.unknown");

        verifyNoInteractions(handleCreated, handleCancelled, handleCompleted);
    }
}
