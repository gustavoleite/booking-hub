package com.bookinghub.catalog.core.domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class EstablishmentTest {

    @Test
    void shouldUpdateDetails() {
        Establishment establishment = Establishment.builder()
                .id(UUID.randomUUID())
                .name("Old Name")
                .description("Old Description")
                .photos(List.of("photo1.jpg"))
                .build();

        String newName = "New Name";
        String newDescription = "New Description";
        List<String> newPhotos = List.of("photo2.jpg", "photo3.jpg");

        establishment.updateDetails(newName, newDescription, newPhotos);

        assertEquals(newName, establishment.getName());
        assertEquals(newDescription, establishment.getDescription());
        assertEquals(newPhotos, establishment.getPhotos());
    }

    @Test
    void shouldInactivate() {
        Establishment establishment = Establishment.builder()
                .id(UUID.randomUUID())
                .active(true)
                .build();

        establishment.inactivate();

        assertFalse(establishment.isActive());
    }

    @Test
    void shouldCreateWithDefaultActiveStatus() {
        Establishment establishment = Establishment.builder()
                .id(UUID.randomUUID())
                .build();

        assertTrue(establishment.isActive());
    }

    @Test
    void shouldHaveGettersForFields() {
        UUID id = UUID.randomUUID();
        String ownerId = "owner-1";
        String cnpj = "12345678000199";
        Address address = Address.builder().street("Street").build();
        List<BusinessHour> hours = List.of();
        List<ProvidedService> services = List.of();

        Establishment establishment = Establishment.builder()
                .id(id)
                .ownerId(ownerId)
                .name("Name")
                .cnpj(cnpj)
                .description("Desc")
                .address(address)
                .defaultBusinessHours(hours)
                .providedServices(services)
                .build();

        assertEquals(id, establishment.getId());
        assertEquals(ownerId, establishment.getOwnerId());
        assertEquals("Name", establishment.getName());
        assertEquals(cnpj, establishment.getCnpj());
        assertEquals("Desc", establishment.getDescription());
        assertEquals(address, establishment.getAddress());
        assertEquals(hours, establishment.getDefaultBusinessHours());
        assertEquals(services, establishment.getProvidedServices());
    }
}
