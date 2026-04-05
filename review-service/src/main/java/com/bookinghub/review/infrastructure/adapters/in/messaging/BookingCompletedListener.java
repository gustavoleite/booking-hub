package com.bookinghub.review.infrastructure.adapters.in.messaging;

import com.bookinghub.review.core.usecases.ConsumeBookingCompletedUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCompletedListener {

    private final ConsumeBookingCompletedUseCase consumeBookingCompletedUseCase;

    @RabbitListener(queues = "#{rabbitMQConfig.reviewBookingCompletedQueueName}")
    public void onBookingCompleted(BookingCompletedEvent event) {
        log.info("Received booking.completed event for booking {}", event.bookingId());
        consumeBookingCompletedUseCase.execute(
                event.bookingId(),
                event.clientId(),
                event.professionalId(),
                event.establishmentId(),
                event.occurredAt());
    }
}
