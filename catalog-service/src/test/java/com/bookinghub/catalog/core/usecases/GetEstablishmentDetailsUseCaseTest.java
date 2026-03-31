package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.exceptions.NotFoundException;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetEstablishmentDetailsUseCaseTest {

    @Mock
    private EstablishmentRepository establishmentRepository;

    @InjectMocks
    private GetEstablishmentDetailsUseCase useCase;

    @Test
    void shouldReturnEstablishmentDetails() {
        UUID id = UUID.randomUUID();
        Establishment establishment = Establishment.builder().id(id).build();
        when(establishmentRepository.findById(id)).thenReturn(Optional.of(establishment));

        Establishment result = useCase.execute(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void shouldThrowNotFoundWhenDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(establishmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> useCase.execute(id));
    }
}
