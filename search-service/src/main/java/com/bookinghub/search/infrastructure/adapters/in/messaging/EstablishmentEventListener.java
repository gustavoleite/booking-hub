package com.bookinghub.search.infrastructure.adapters.in.messaging;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.usecases.IndexEstablishmentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstablishmentEventListener {

    private final IndexEstablishmentUseCase indexEstablishmentUseCase;

    @RabbitListener(queues = "#{rabbitMQConfig.searchEstablishmentCreatedQueueName}")
    public void onEstablishmentCreated(EstablishmentEvent event) {
        log.info("Received establishment.created for {}", event.id());
        indexEstablishmentUseCase.execute(toDocument(event));
    }

    @RabbitListener(queues = "#{rabbitMQConfig.searchEstablishmentUpdatedQueueName}")
    public void onEstablishmentUpdated(EstablishmentEvent event) {
        log.info("Received establishment.updated for {}", event.id());
        indexEstablishmentUseCase.execute(toDocument(event));
    }

    private EstablishmentDocument toDocument(EstablishmentEvent event) {
        return EstablishmentDocument.builder()
                .id(event.id())
                .name(event.name())
                .description(event.description())
                .city(event.city())
                .state(event.state())
                .zipCode(event.zipCode())
                .lat(event.latitude() != null ? event.latitude().doubleValue() : null)
                .lon(event.longitude() != null ? event.longitude().doubleValue() : null)
                .services(Collections.emptyList())
                .professionals(Collections.emptyList())
                .totalReviews(0)
                .build();
    }
}
