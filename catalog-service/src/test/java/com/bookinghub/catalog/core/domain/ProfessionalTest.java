package com.bookinghub.catalog.core.domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProfessionalTest {

    @Test
    void shouldUpdateProfile() {
        Professional professional = Professional.builder()
                .id(UUID.randomUUID())
                .name("Old Name")
                .bio("Old Bio")
                .avatarUrl("old.jpg")
                .specialties(List.of("A"))
                .build();

        String newName = "New Name";
        String newBio = "New Bio";
        String newAvatar = "new.jpg";
        List<String> newSpecs = List.of("B", "C");

        professional.updateProfile(newName, newBio, newAvatar, newSpecs);

        assertEquals(newName, professional.getName());
        assertEquals(newBio, professional.getBio());
        assertEquals(newAvatar, professional.getAvatarUrl());
        assertEquals(newSpecs, professional.getSpecialties());
    }

    @Test
    void shouldInactivate() {
        Professional professional = Professional.builder()
                .active(true)
                .build();

        professional.inactivate();

        assertFalse(professional.isActive());
    }

    @Test
    void shouldHaveGetters() {
        UUID id = UUID.randomUUID();
        Professional professional = Professional.builder()
                .id(id)
                .name("Name")
                .bio("Bio")
                .avatarUrl("url")
                .specialties(List.of())
                .build();

        assertEquals(id, professional.getId());
        assertEquals("Name", professional.getName());
        assertEquals("Bio", professional.getBio());
        assertEquals("url", professional.getAvatarUrl());
        assertEquals(List.of(), professional.getSpecialties());
    }
}
