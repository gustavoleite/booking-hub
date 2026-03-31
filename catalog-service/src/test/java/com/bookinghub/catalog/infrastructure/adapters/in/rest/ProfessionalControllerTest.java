package com.bookinghub.catalog.infrastructure.adapters.in.rest;

import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import com.bookinghub.catalog.core.usecases.UpsertProfessionalProfileUseCase;
import com.bookinghub.catalog.infrastructure.adapters.in.rest.dto.ProfessionalProfileRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionalControllerTest {

    @Mock private UpsertProfessionalProfileUseCase upsertProfessionalProfileUseCase;
    @Mock private ProfessionalRepository professionalRepository;

    @InjectMocks
    private ProfessionalController controller;

    @Test
    void shouldUpsertProfile() {
        UUID userId = UUID.randomUUID();
        ProfessionalProfileRequest request = new ProfessionalProfileRequest();
        request.setName("Pro");
        Professional saved = Professional.builder().id(userId).name("Pro").build();
        when(upsertProfessionalProfileUseCase.execute(eq(userId), any())).thenReturn(saved);

        ResponseEntity<Professional> response = controller.upsertMyProfile(userId.toString(), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(saved, response.getBody());
    }

    @Test
    void shouldGetMyProfile() {
        UUID userId = UUID.randomUUID();
        Professional professional = Professional.builder().id(userId).build();
        when(professionalRepository.findById(userId)).thenReturn(Optional.of(professional));

        ResponseEntity<Professional> response = controller.getMyProfile(userId.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(professional, response.getBody());
    }

    @Test
    void shouldReturnNotFoundWhenMyProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(professionalRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseEntity<Professional> response = controller.getMyProfile(userId.toString());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldThrowExceptionWhenInvalidUserId() {
        assertThrows(BusinessRuleException.class, () -> controller.getMyProfile("invalid-uuid"));
    }

    @Test
    void shouldGetProfileById() {
        UUID id = UUID.randomUUID();
        Professional professional = Professional.builder().id(id).build();
        when(professionalRepository.findById(id)).thenReturn(Optional.of(professional));

        ResponseEntity<Professional> response = controller.getProfile(id.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(professional, response.getBody());
    }

    @Test
    void shouldReturnNotFoundWhenProfileByIdDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(professionalRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<Professional> response = controller.getProfile(id.toString());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnNotFoundWhenInvalidProfileId() {
        ResponseEntity<Professional> response = controller.getProfile("invalid-uuid");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
