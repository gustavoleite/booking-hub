package com.bookinghub.catalog.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ServiceOffering {
    private final UUID id;
    private final UUID providedServiceId;
    private final BigDecimal price;
    private final int durationMinutes;
}
