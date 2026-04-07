package com.bookinghub.catalog.core.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceOfferingTest {

    @Test
    void shouldHaveGetters() {
        UUID id = UUID.randomUUID();
        UUID provId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("100.00");

        ServiceOffering offering = ServiceOffering.builder()
                .id(id)
                .providedServiceId(provId)
                .price(price)
                .durationMinutes(60)
                .build();

        assertEquals(id, offering.getId());
        assertEquals(provId, offering.getProvidedServiceId());
        assertEquals(price, offering.getPrice());
        assertEquals(60, offering.getDurationMinutes());
    }
}
