package com.bookinghub.search.infrastructure.adapters.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EstablishmentEvent(
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
