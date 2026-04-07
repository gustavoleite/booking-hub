package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.domain.ProvidedService;
import com.bookinghub.catalog.core.exceptions.ForbiddenException;
import com.bookinghub.catalog.core.exceptions.NotFoundException;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddProvidedServiceUseCaseTest {

    @Mock
    private EstablishmentRepository establishmentRepository;

    @InjectMocks
    private AddProvidedServiceUseCase useCase;

    @Test
    void shouldAddProvidedService() {
        UUID establishmentId = UUID.randomUUID();
        String ownerId = "owner-1";
        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .ownerId(ownerId)
                .providedServices(new ArrayList<>())
                .build();
        
        ProvidedService serviceToRequest = ProvidedService.builder()
                .title("New Service")
                .description("Desc")
                .build();

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));

        ProvidedService result = useCase.execute(establishmentId, ownerId, serviceToRequest);

        assertNotNull(result);
        assertEquals("New Service", result.getTitle());
        assertEquals(1, establishment.getProvidedServices().size());
        verify(establishmentRepository).save(establishment);
    }

    @Test
    void shouldThrowNotFoundWhenEstablishmentDoesNotExist() {
        UUID establishmentId = UUID.randomUUID();
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, 
            () -> useCase.execute(establishmentId, "owner-1", ProvidedService.builder().build()));
    }

    @Test
    void shouldThrowForbiddenWhenNotOwner() {
        UUID establishmentId = UUID.randomUUID();
        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .ownerId("owner-1")
                .build();
        
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));

        assertThrows(ForbiddenException.class, 
            () -> useCase.execute(establishmentId, "wrong-owner", ProvidedService.builder().build()));
    }

    @Test
    void shouldHandleNullProvidedServicesList() {
        UUID establishmentId = UUID.randomUUID();
        String ownerId = "owner-1";
        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .ownerId(ownerId)
                .providedServices(null)
                .build();
        
        ProvidedService serviceToRequest = ProvidedService.builder()
                .title("New Service")
                .build();

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));

        ProvidedService result = useCase.execute(establishmentId, ownerId, serviceToRequest);
        
        assertNotNull(result);
        assertNotNull(establishment.getProvidedServices());
        assertEquals(1, establishment.getProvidedServices().size());
    }
}
