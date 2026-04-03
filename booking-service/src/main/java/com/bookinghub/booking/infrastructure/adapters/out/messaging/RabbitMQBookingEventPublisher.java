package com.bookinghub.booking.infrastructure.adapters.out.messaging;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.ports.BookingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQBookingEventPublisher implements BookingEventPublisher {

    private static final String EXCHANGE = "booking.events";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishBookingCreated(Booking booking) {
        publish("booking.created", booking);
    }

    @Override
    public void publishBookingCancelled(Booking booking) {
        publish("booking.cancelled", booking);
    }

    @Override
    public void publishBookingCompleted(Booking booking) {
        publish("booking.completed", booking);
    }

    private void publish(String routingKey, Booking booking) {
        BookingEventPayload payload = new BookingEventPayload(
                booking.getId(),
                booking.getClientId(),
                booking.getProfessionalId(),
                booking.getEstablishmentId(),
                booking.getProvidedServiceId(),
                booking.getStartDatetime(),
                booking.getEndDatetime(),
                booking.getPrice(),
                booking.getDurationMinutes(),
                booking.getStatus().name(),
                LocalDateTime.now()
        );
        log.info("Publishing event [{}] for booking {}", routingKey, booking.getId());
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload);
    }
}
