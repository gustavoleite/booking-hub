package com.bookinghub.catalog.core.domain;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ServiceOffering {
  private final UUID id;
  private final UUID providedServiceId;
  private final BigDecimal price;
  private final int durationMinutes;
}
