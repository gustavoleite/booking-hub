package com.bookinghub.catalog.infrastructure.adapters.in.rest;

import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.usecases.AddProfessionalToEstablishmentUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AffiliationControllerTest {

    @Mock
    private AddProfessionalToEstablishmentUseCase addProfessionalToEstablishmentUseCase;

    @InjectMocks
    private AffiliationController controller;

    @Test
    void shouldAddProfessional() {
        UUID estId = UUID.randomUUID();
        UUID profId = UUID.randomUUID();
        Affiliation affiliation = Affiliation.builder().build();
        Affiliation saved = Affiliation.builder().id(UUID.randomUUID()).build();
        
        when(addProfessionalToEstablishmentUseCase.execute(eq(estId), eq(profId), eq(affiliation))).thenReturn(saved);

        ResponseEntity<Affiliation> response = controller.addProfessional(estId, profId, affiliation);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(saved, response.getBody());
    }
}
