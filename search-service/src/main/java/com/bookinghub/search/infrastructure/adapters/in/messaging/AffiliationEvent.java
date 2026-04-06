package com.bookinghub.search.infrastructure.adapters.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AffiliationEvent(
        String affiliationId,
        String establishmentId,
        String professionalId,
        String professionalName,
        List<String> professionalSpecialties,
        boolean active,
        List<ServiceOfferingEvent> serviceOfferings
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ServiceOfferingEvent(
            String providedServiceId,
            String serviceTitle,
            BigDecimal price,
            int durationMinutes
    ) {}
}
