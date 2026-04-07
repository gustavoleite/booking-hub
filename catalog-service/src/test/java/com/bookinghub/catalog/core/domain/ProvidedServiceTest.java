package com.bookinghub.catalog.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

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
