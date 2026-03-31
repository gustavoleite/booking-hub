package com.bookinghub.catalog.core.domain;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ProvidedServiceTest {

    @Test
    void shouldInactivate() {
        ProvidedService service = ProvidedService.builder()
                .active(true)
                .build();

        service.inactivate();

        assertFalse(service.isActive());
    }

    @Test
    void shouldHaveGetters() {
        UUID id = UUID.randomUUID();
        ProvidedService service = ProvidedService.builder()
                .id(id)
                .title("Title")
                .description("Desc")
                .build();

        assertEquals(id, service.getId());
        assertEquals("Title", service.getTitle());
        assertEquals("Desc", service.getDescription());
        assertTrue(service.isActive());
    }
}
