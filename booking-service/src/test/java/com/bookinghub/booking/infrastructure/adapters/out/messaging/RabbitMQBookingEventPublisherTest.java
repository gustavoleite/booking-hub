package com.bookinghub.booking.infrastructure.adapters.out.messaging;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.domain.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQBookingEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQBookingEventPublisher publisher;

    private Booking buildBooking() {
        return Booking.builder()
                .id(UUID.randomUUID())
                .clientId("client1")
                .professionalId(UUID.randomUUID())
                .establishmentId(UUID.randomUUID())
                .providedServiceId(UUID.randomUUID())
                .startDatetime(LocalDateTime.now().plusDays(1))
                .endDatetime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(BookingStatus.CONFIRMED)
                .price(new BigDecimal("50.00"))
                .durationMinutes(60)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldPublishBookingCreated() {
        Booking booking = buildBooking();
        publisher.publishBookingCreated(booking);
        verify(rabbitTemplate).convertAndSend(eq("booking.events"), eq("booking.created"), any(BookingEventPayload.class));
    }

    @Test
    void shouldPublishBookingCancelled() {
        Booking booking = buildBooking();
        publisher.publishBookingCancelled(booking);
        verify(rabbitTemplate).convertAndSend(eq("booking.events"), eq("booking.cancelled"), any(BookingEventPayload.class));
    }

    @Test
    void shouldPublishBookingCompleted() {
        Booking booking = buildBooking();
        publisher.publishBookingCompleted(booking);
        verify(rabbitTemplate).convertAndSend(eq("booking.events"), eq("booking.completed"), any(BookingEventPayload.class));
    }
}
