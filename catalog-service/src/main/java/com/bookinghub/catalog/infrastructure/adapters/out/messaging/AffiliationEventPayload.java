package com.bookinghub.catalog.infrastructure.adapters.out.messaging;

import java.math.BigDecimal;
import java.util.List;

public record AffiliationEventPayload(
        String affiliationId,
        String establishmentId,
        String professionalId,
        String professionalName,
        List<String> professionalSpecialties,
        boolean active,
        List<ServiceOfferingPayload> serviceOfferings
) {
    public record ServiceOfferingPayload(
            String providedServiceId,
            String serviceTitle,
            BigDecimal price,
            int durationMinutes
  ) {
    }
}
