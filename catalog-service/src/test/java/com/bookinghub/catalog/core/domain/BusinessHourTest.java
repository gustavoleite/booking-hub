package com.bookinghub.catalog.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class BusinessHourTest {

    @Test
    void shouldBeWithinRange() {
        BusinessHour businessHour = BusinessHour.builder()
                .dayOfWeek(1)
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(18, 0))
                .build();

        assertTrue(businessHour.isWithin(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        assertTrue(businessHour.isWithin(LocalTime.of(9, 0), LocalTime.of(17, 0)));
    }

    @Test
    void shouldNotBeWithinRange() {
        BusinessHour businessHour = BusinessHour.builder()
                .dayOfWeek(1)
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(18, 0))
                .build();

        assertFalse(businessHour.isWithin(LocalTime.of(7, 59), LocalTime.of(18, 0)));
        assertFalse(businessHour.isWithin(LocalTime.of(8, 0), LocalTime.of(18, 1)));
        assertFalse(businessHour.isWithin(LocalTime.of(7, 0), LocalTime.of(19, 0)));
    }

    @Test
    void shouldHaveGetters() {
        LocalTime open = LocalTime.of(8, 0);
        LocalTime close = LocalTime.of(18, 0);
        BusinessHour businessHour = BusinessHour.builder()
                .dayOfWeek(1)
                .openTime(open)
                .closeTime(close)
                .build();

        assertEquals(1, businessHour.getDayOfWeek());
        assertEquals(open, businessHour.getOpenTime());
        assertEquals(close, businessHour.getCloseTime());
    }
}
