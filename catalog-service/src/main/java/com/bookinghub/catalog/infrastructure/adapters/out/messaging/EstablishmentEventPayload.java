package com.bookinghub.catalog.infrastructure.adapters.out.messaging;

import java.math.BigDecimal;

public record EstablishmentEventPayload(
        String id,
        String name,
        String description,
        String city,
        String state,
        String zipCode,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
