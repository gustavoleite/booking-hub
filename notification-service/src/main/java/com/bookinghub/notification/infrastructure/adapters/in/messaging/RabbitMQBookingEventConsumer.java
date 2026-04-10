package com.bookinghub.notification.infrastructure.adapters.in.messaging;

import com.bookinghub.notification.core.usecases.HandleBookingCancelledUseCase;
import com.bookinghub.notification.core.usecases.HandleBookingCompletedUseCase;
import com.bookinghub.notification.core.usecases.HandleBookingCreatedUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQBookingEventConsumer {

  private static final String BOOKING_CREATED = "booking.created";
  private static final String BOOKING_CANCELLED = "booking.cancelled";
  private static final String BOOKING_COMPLETED = "booking.completed";

  private final HandleBookingCreatedUseCase handleCreated;
  private final HandleBookingCancelledUseCase handleCancelled;
  private final HandleBookingCompletedUseCase handleCompleted;

  @RabbitListener(queues = "calendar.sync.queue")
  public void handle(
      BookingEventPayload payload,
      @Header("amqp_receivedRoutingKey") String routingKey) {

    log.info("Received event [{}] for booking {}", routingKey, payload.bookingId());

    switch (routingKey) {
      case BOOKING_CREATED -> handleCreated.execute(
          payload.bookingId(),
          payload.clientId(),
          payload.professionalId(),
          payload.startDatetime(),
          payload.endDatetime(),
          payload.clientEmail(),
          payload.professionalEmail());
      case BOOKING_CANCELLED -> handleCancelled.execute(payload.bookingId());
      case BOOKING_COMPLETED -> handleCompleted.execute(payload.bookingId());
      default -> log.warn("Unhandled routing key: {}", routingKey);
    }
  }
}
