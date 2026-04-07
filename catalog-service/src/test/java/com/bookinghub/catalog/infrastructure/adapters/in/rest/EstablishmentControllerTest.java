package com.bookinghub.catalog.infrastructure.adapters.in.rest;

import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.domain.ProvidedService;
import com.bookinghub.catalog.core.usecases.*;
import com.bookinghub.catalog.infrastructure.adapters.in.rest.dto.EstablishmentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstablishmentControllerTest {

    @Mock private CreateEstablishmentUseCase createEstablishmentUseCase;
    @Mock private UpdateEstablishmentUseCase updateEstablishmentUseCase;
    @Mock private InactivateEstablishmentUseCase inactivateEstablishmentUseCase;
    @Mock private GetEstablishmentDetailsUseCase getEstablishmentDetailsUseCase;
    @Mock private ListMyEstablishmentsUseCase listMyEstablishmentsUseCase;
    @Mock private AddProvidedServiceUseCase addProvidedServiceUseCase;

    @InjectMocks
    private EstablishmentController controller;

    private String ownerId = "owner-123";
    private UUID id = UUID.randomUUID();

    @Test
    void shouldCreateEstablishment() {
        EstablishmentRequest request = new EstablishmentRequest();
        request.setName("Salon");
        Establishment saved = Establishment.builder().id(id).name("Salon").build();
        when(createEstablishmentUseCase.execute(any())).thenReturn(saved);

        ResponseEntity<Establishment> response = controller.create(ownerId, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saved, response.getBody());
        verify(createEstablishmentUseCase).execute(any());
    }

    @Test
    void shouldListMySalons() {
        List<Establishment> list = List.of(Establishment.builder().id(id).build());
        when(listMyEstablishmentsUseCase.execute(ownerId)).thenReturn(list);

        ResponseEntity<List<Establishment>> response = controller.listMySalons(ownerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody());
    }

    @Test
    void shouldGetDetails() {
        Establishment establishment = Establishment.builder().id(id).build();
        when(getEstablishmentDetailsUseCase.execute(id)).thenReturn(establishment);

        ResponseEntity<Establishment> response = controller.getDetails(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(establishment, response.getBody());
    }

    @Test
    void shouldUpdateEstablishment() {
        EstablishmentRequest request = new EstablishmentRequest();
        request.setName("Updated");
        Establishment updated = Establishment.builder().id(id).name("Updated").build();
        when(updateEstablishmentUseCase.execute(eq(id), eq(ownerId), any())).thenReturn(updated);

        ResponseEntity<Establishment> response = controller.update(id, ownerId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
    }

    @Test
    void shouldInactivateEstablishment() {
        ResponseEntity<Void> response = controller.delete(id, ownerId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(inactivateEstablishmentUseCase).execute(id, ownerId);
    }

    @Test
    void shouldAddService() {
        EstablishmentRequest.ProvidedServiceDto dto = new EstablishmentRequest.ProvidedServiceDto();
        dto.setTitle("Service");
        ProvidedService saved = ProvidedService.builder().title("Service").build();
        when(addProvidedServiceUseCase.execute(eq(id), eq(ownerId), any())).thenReturn(saved);

        ResponseEntity<ProvidedService> response = controller.addService(id, ownerId, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(saved, response.getBody());
    }
}
