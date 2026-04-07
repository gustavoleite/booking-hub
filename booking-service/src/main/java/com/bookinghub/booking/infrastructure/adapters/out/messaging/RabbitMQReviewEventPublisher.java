package com.bookinghub.booking.infrastructure.adapters.out.messaging;

import com.bookinghub.booking.core.domain.Review;
import com.bookinghub.booking.core.ports.ReviewEventPublisher;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQReviewEventPublisher implements ReviewEventPublisher {

    private static final String EXCHANGE = "review.events";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishReviewCreated(Review review) {
        ReviewEventPayload payload = new ReviewEventPayload(
                review.getId(),
                review.getBookingId(),
                review.getClientId(),
                review.getProfessionalId(),
                review.getEstablishmentId(),
                review.getProfessionalRating(),
                review.getEstablishmentRating(),
                LocalDateTime.now());
        if (log.isInfoEnabled()) {
            log.info("Publishing event [review.created] for review {}", review.getId());
        }
        rabbitTemplate.convertAndSend(EXCHANGE, "review.created", payload);
    }
}
