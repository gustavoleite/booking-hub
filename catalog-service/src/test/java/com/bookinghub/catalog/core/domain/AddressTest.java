package com.bookinghub.catalog.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AddressTest {

    @Test
    void shouldHaveGetters() {
        Address address = Address.builder()
                .street("Main St")
                .number("123")
                .zipCode("12345-678")
                .latitude(new BigDecimal("-23.5505"))
                .longitude(new BigDecimal("-46.6333"))
                .build();

        assertEquals("Main St", address.getStreet());
        assertEquals("123", address.getNumber());
        assertEquals("12345-678", address.getZipCode());
        assertEquals(new BigDecimal("-23.5505"), address.getLatitude());
        assertEquals(new BigDecimal("-46.6333"), address.getLongitude());
    }
}
