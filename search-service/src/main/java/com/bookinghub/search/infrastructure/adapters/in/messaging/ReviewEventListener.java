package com.bookinghub.search.infrastructure.adapters.in.messaging;

import com.bookinghub.search.core.usecases.IndexReviewUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventListener {

    private final IndexReviewUseCase indexReviewUseCase;

    @RabbitListener(queues = "#{rabbitMQConfig.searchReviewCreatedQueueName}")
    public void onReviewCreated(ReviewEvent event) {
        log.info("Received review.created for establishment {}", event.establishmentId());
        indexReviewUseCase.execute(event.establishmentId(), event.establishmentRating());
    }
}
