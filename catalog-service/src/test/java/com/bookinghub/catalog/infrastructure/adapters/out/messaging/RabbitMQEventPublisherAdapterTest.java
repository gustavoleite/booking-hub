package com.bookinghub.catalog.infrastructure.adapters.out.messaging;

import com.bookinghub.catalog.core.domain.Affiliation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMQEventPublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQEventPublisherAdapter adapter;

    @Test
    void shouldPublishAffiliationCreated() {
        Affiliation affiliation = Affiliation.builder().build();
        adapter.publishAffiliationCreated(affiliation);
        verify(rabbitTemplate).convertAndSend("catalog.events", "affiliation.created", affiliation);
    }
}
