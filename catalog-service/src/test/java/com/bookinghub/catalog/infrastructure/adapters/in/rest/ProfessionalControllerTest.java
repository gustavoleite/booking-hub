package com.bookinghub.catalog.infrastructure.adapters.in.rest;

import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.exceptions.ProfessionalNotFoundException;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import com.bookinghub.catalog.core.usecases.CreateProfessionalProfileUseCase;
import com.bookinghub.catalog.core.usecases.UpdateProfessionalProfileUseCase;
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

    @Mock private CreateProfessionalProfileUseCase createProfessionalProfileUseCase;
    @Mock private UpdateProfessionalProfileUseCase updateProfessionalProfileUseCase;
    @Mock private ProfessionalRepository professionalRepository;

    @InjectMocks
    private ProfessionalController controller;

    @Test
    void shouldCreateProfile() {
        UUID userId = UUID.randomUUID();
        ProfessionalProfileRequest request = new ProfessionalProfileRequest();
        request.setName("Pro");
        Professional saved = Professional.builder().id(userId).name("Pro").build();
        when(createProfessionalProfileUseCase.execute(eq(userId), any())).thenReturn(saved);

        ResponseEntity<Professional> response = controller.createMyProfile(userId.toString(), request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saved, response.getBody());
    }

    @Test
    void shouldUpdateProfile() {
        UUID userId = UUID.randomUUID();
        ProfessionalProfileRequest request = new ProfessionalProfileRequest();
        request.setName("Pro Updated");
        Professional updated = Professional.builder().id(userId).name("Pro Updated").build();
        when(updateProfessionalProfileUseCase.execute(eq(userId), any())).thenReturn(updated);

        ResponseEntity<Professional> response = controller.updateMyProfile(userId.toString(), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
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
    void shouldThrowExceptionWhenMyProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(professionalRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ProfessionalNotFoundException.class, () -> controller.getMyProfile(userId.toString()));
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
    void shouldThrowExceptionWhenProfileByIdDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(professionalRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProfessionalNotFoundException.class, () -> controller.getProfile(id.toString()));
    }

    @Test
    void shouldThrowExceptionWhenInvalidProfileId() {
        assertThrows(ProfessionalNotFoundException.class, () -> controller.getProfile("invalid-uuid"));
    }
}
