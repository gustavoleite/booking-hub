package com.bookinghub.catalog.infrastructure.adapters.out.messaging;

import com.bookinghub.catalog.core.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQEventPublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQEventPublisherAdapter adapter;

    @Test
    void shouldPublishEstablishmentCreated() {
        Establishment establishment = buildEstablishment();

        adapter.publishEstablishmentCreated(establishment);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("catalog.events"), eq("establishment.created"), payloadCaptor.capture());

        Object payload = payloadCaptor.getValue();
        assertInstanceOf(EstablishmentEventPayload.class, payload);
        EstablishmentEventPayload ep = (EstablishmentEventPayload) payload;
        assertEquals("São Paulo", ep.city());
        assertEquals("SP", ep.state());
        assertEquals(new BigDecimal("-23.5505"), ep.latitude());
        assertEquals(new BigDecimal("-46.6333"), ep.longitude());
    }

    @Test
    void shouldPublishEstablishmentUpdated() {
        Establishment establishment = buildEstablishment();

        adapter.publishEstablishmentUpdated(establishment);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("catalog.events"), eq("establishment.updated"), payloadCaptor.capture());

        Object payload = payloadCaptor.getValue();
        assertInstanceOf(EstablishmentEventPayload.class, payload);
    }

    @Test
    void shouldPublishAffiliationCreated() {
        UUID providedServiceId = UUID.randomUUID();
        Establishment establishment = buildEstablishmentWithService(providedServiceId);
        Professional professional = Professional.builder()
                .id(UUID.randomUUID())
                .name("Dr. Jane")
                .specialties(List.of("Haircut"))
                .build();
        Affiliation affiliation = buildAffiliation(UUID.randomUUID(), UUID.randomUUID(), providedServiceId);

        adapter.publishAffiliationCreated(affiliation, professional, establishment);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("catalog.events"), eq("affiliation.created"), payloadCaptor.capture());

        Object payload = payloadCaptor.getValue();
        assertInstanceOf(AffiliationEventPayload.class, payload);
        AffiliationEventPayload ap = (AffiliationEventPayload) payload;
        assertEquals("Dr. Jane", ap.professionalName());
        assertEquals(List.of("Haircut"), ap.professionalSpecialties());
        assertFalse(ap.serviceOfferings().isEmpty());
        assertEquals("Haircut Service", ap.serviceOfferings().get(0).serviceTitle());
    }

    @Test
    void shouldPublishAffiliationUpdated() {
        UUID providedServiceId = UUID.randomUUID();
        Establishment establishment = buildEstablishmentWithService(providedServiceId);
        Professional professional = Professional.builder()
                .id(UUID.randomUUID())
                .name("Dr. John")
                .specialties(List.of("Styling"))
                .build();
        Affiliation affiliation = buildAffiliation(UUID.randomUUID(), UUID.randomUUID(), providedServiceId);

        adapter.publishAffiliationUpdated(affiliation, professional, establishment);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("catalog.events"), eq("affiliation.updated"), payloadCaptor.capture());

        Object payload = payloadCaptor.getValue();
        assertInstanceOf(AffiliationEventPayload.class, payload);
        AffiliationEventPayload ap = (AffiliationEventPayload) payload;
        assertEquals("Dr. John", ap.professionalName());
    }

    private Establishment buildEstablishment() {
        return Establishment.builder()
                .id(UUID.randomUUID())
                .name("Test Salon")
                .description("A test salon")
                .address(Address.builder()
                        .city("São Paulo")
                        .state("SP")
                        .zipCode("01310-100")
                        .latitude(new BigDecimal("-23.5505"))
                        .longitude(new BigDecimal("-46.6333"))
                        .build())
                .build();
    }

    private Establishment buildEstablishmentWithService(UUID providedServiceId) {
        return Establishment.builder()
                .id(UUID.randomUUID())
                .name("Test Salon")
                .address(Address.builder()
                        .city("São Paulo")
                        .state("SP")
                        .latitude(new BigDecimal("-23.5505"))
                        .longitude(new BigDecimal("-46.6333"))
                        .build())
                .providedServices(List.of(
                        ProvidedService.builder()
                                .id(providedServiceId)
                                .title("Haircut Service")
                                .build()
                ))
                .build();
    }

    private Affiliation buildAffiliation(UUID affiliationId, UUID professionalId, UUID providedServiceId) {
        return Affiliation.builder()
                .id(affiliationId)
                .establishmentId(UUID.randomUUID())
                .professionalId(professionalId)
                .active(true)
                .serviceOfferings(List.of(
                        ServiceOffering.builder()
                                .id(UUID.randomUUID())
                                .providedServiceId(providedServiceId)
                                .price(new BigDecimal("50.00"))
                                .durationMinutes(30)
                                .build()
                ))
                .build();
    }
}
