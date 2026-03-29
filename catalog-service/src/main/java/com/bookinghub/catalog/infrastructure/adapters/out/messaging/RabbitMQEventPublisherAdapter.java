package com.bookinghub.catalog.infrastructure.adapters.out.messaging;

import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.ports.CatalogEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQEventPublisherAdapter implements CatalogEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishAffiliationCreated(Affiliation affiliation) {
        rabbitTemplate.convertAndSend("catalog.events", "affiliation.created", affiliation);
    }
}
