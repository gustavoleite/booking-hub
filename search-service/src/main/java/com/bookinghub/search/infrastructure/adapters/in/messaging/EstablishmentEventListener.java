package com.bookinghub.search.infrastructure.adapters.in.messaging;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.usecases.IndexEstablishmentUseCase;
import com.bookinghub.search.core.usecases.UpdateEstablishmentUseCase;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstablishmentEventListener {

  private final IndexEstablishmentUseCase indexEstablishmentUseCase;
  private final UpdateEstablishmentUseCase updateEstablishmentUseCase;

  @RabbitListener(queues = "#{rabbitMQConfig.searchEstablishmentCreatedQueueName}")
  public void onEstablishmentCreated(EstablishmentEvent event) {
    if (log.isInfoEnabled()) {
      log.info("Received establishment.created for {}", event.id());
    }
    indexEstablishmentUseCase.execute(toDocument(event));
  }

  @RabbitListener(queues = "#{rabbitMQConfig.searchEstablishmentUpdatedQueueName}")
  public void onEstablishmentUpdated(EstablishmentEvent event) {
    if (log.isInfoEnabled()) {
      log.info("Received establishment.updated for {}", event.id());
    }
    updateEstablishmentUseCase.execute(
        event.id(),
        event.name(),
        event.description(),
        event.city(),
        event.state(),
        event.zipCode(),
        event.latitude() != null ? event.latitude().doubleValue() : null,
        event.longitude() != null ? event.longitude().doubleValue() : null
    );
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
