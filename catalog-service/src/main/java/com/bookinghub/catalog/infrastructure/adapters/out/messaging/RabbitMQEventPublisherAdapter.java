package com.bookinghub.catalog.infrastructure.adapters.out.messaging;

import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.ports.CatalogEventPublisher;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQEventPublisherAdapter implements CatalogEventPublisher {

    private static final String EXCHANGE = "catalog.events";
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishEstablishmentCreated(Establishment establishment) {
        rabbitTemplate.convertAndSend(
                EXCHANGE, "establishment.created", toEstablishmentPayload(establishment));
    }

    @Override
    public void publishEstablishmentUpdated(Establishment establishment) {
        rabbitTemplate.convertAndSend(
                EXCHANGE, "establishment.updated", toEstablishmentPayload(establishment));
    }

    @Override
    public void publishAffiliationCreated(
      Affiliation affiliation, Professional professional, Establishment establishment) {
        rabbitTemplate.convertAndSend(
                EXCHANGE, "affiliation.created",
                toAffiliationPayload(affiliation, professional, establishment));
    }

    @Override
    public void publishAffiliationUpdated(
      Affiliation affiliation, Professional professional, Establishment establishment) {
        rabbitTemplate.convertAndSend(
                EXCHANGE, "affiliation.updated",
                toAffiliationPayload(affiliation, professional, establishment));
    }

    private EstablishmentEventPayload toEstablishmentPayload(Establishment e) {
        var address = e.getAddress();
        return new EstablishmentEventPayload(
                e.getId() != null ? e.getId().toString() : null,
                e.getName(),
                e.getDescription(),
                address != null ? address.getCity() : null,
                address != null ? address.getState() : null,
                address != null ? address.getZipCode() : null,
                address != null ? address.getLatitude() : null,
                address != null ? address.getLongitude() : null
        );
    }

    private AffiliationEventPayload toAffiliationPayload(
      Affiliation affiliation, Professional professional, Establishment establishment) {
        // Build a map of providedServiceId -> title from the establishment
        Map<String, String> serviceTitles = establishment.getProvidedServices() != null
                ? establishment.getProvidedServices().stream()
                .collect(Collectors.toMap(s -> s.getId().toString(), s -> s.getTitle()))
                : Collections.emptyMap();

        List<AffiliationEventPayload.ServiceOfferingPayload> offerings =
                affiliation.getServiceOfferings() != null
                        ? affiliation.getServiceOfferings().stream()
                        .map(so -> new AffiliationEventPayload.ServiceOfferingPayload(
                                so.getProvidedServiceId().toString(),
                                serviceTitles.getOrDefault(
                                        so.getProvidedServiceId().toString(), ""),
                                so.getPrice(),
                                so.getDurationMinutes()
                        ))
                        .toList()
                        : Collections.emptyList();

        return new AffiliationEventPayload(
                affiliation.getId() != null ? affiliation.getId().toString() : null,
                affiliation.getEstablishmentId() != null
                        ? affiliation.getEstablishmentId().toString() : null,
                affiliation.getProfessionalId() != null
                        ? affiliation.getProfessionalId().toString() : null,
                professional.getName(),
                professional.getSpecialties() != null
                        ? professional.getSpecialties() : Collections.emptyList(),
                affiliation.isActive(),
                offerings
        );
    }
}
