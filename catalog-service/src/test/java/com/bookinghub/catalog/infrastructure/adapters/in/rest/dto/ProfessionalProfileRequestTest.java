package com.bookinghub.catalog.infrastructure.adapters.in.rest.dto;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfessionalProfileRequestTest {

    @Test
    void testGettersSetters() {
        ProfessionalProfileRequest request = new ProfessionalProfileRequest();
        request.setName("Name");
        request.setBio("Bio");
        request.setAvatarUrl("url");
        request.setSpecialties(List.of("Spec"));

        assertEquals("Name", request.getName());
        assertEquals("Bio", request.getBio());
        assertEquals("url", request.getAvatarUrl());
        assertEquals(1, request.getSpecialties().size());
    }
}
