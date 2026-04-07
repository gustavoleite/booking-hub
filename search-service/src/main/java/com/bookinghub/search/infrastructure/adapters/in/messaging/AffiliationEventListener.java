package com.bookinghub.search.infrastructure.adapters.in.messaging;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.usecases.IndexAffiliationUseCase;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AffiliationEventListener {

    private final IndexAffiliationUseCase indexAffiliationUseCase;

    @RabbitListener(queues = "#{rabbitMQConfig.searchAffiliationCreatedQueueName}")
    public void onAffiliationCreated(AffiliationEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Received affiliation.created for establishment {}", event.establishmentId());
        }
        process(event);
    }

    @RabbitListener(queues = "#{rabbitMQConfig.searchAffiliationUpdatedQueueName}")
    public void onAffiliationUpdated(AffiliationEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Received affiliation.updated for establishment {}", event.establishmentId());
        }
        process(event);
    }

    private void process(AffiliationEvent event) {
        List<EstablishmentDocument.ServiceEntry> offerings = event.serviceOfferings() != null
                ? event.serviceOfferings().stream()
                .map(so -> EstablishmentDocument.ServiceEntry.builder()
                .serviceId(so.providedServiceId())
                .title(so.serviceTitle())
                .minPrice(so.price() != null ? so.price().doubleValue() : null)
                .maxPrice(so.price() != null ? so.price().doubleValue() : null)
                .build())
                .toList()
                : Collections.emptyList();

        indexAffiliationUseCase.execute(
                event.establishmentId(),
                event.professionalId(),
                event.professionalName(),
                event.professionalSpecialties() != null
                        ? event.professionalSpecialties() : Collections.emptyList(),
                event.active(),
                offerings
        );
    }
}
