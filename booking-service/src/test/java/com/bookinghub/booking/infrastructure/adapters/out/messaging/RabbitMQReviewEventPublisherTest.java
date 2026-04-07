package com.bookinghub.booking.infrastructure.adapters.out.messaging;

import com.bookinghub.booking.core.domain.Review;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQReviewEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQReviewEventPublisher publisher;

    @Test
    void shouldPublishReviewCreated() {
        Review review = Review.builder()
                .id(UUID.randomUUID())
                .bookingId(UUID.randomUUID())
                .clientId("c1")
                .professionalId(UUID.randomUUID())
                .establishmentId(UUID.randomUUID())
                .professionalRating(5)
                .establishmentRating(5)
                .build();

        publisher.publishReviewCreated(review);

        verify(rabbitTemplate).convertAndSend(eq("review.events"), eq("review.created"), any(ReviewEventPayload.class));
    }
}
