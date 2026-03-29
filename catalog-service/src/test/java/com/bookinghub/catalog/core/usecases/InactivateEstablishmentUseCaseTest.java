package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.exceptions.ForbiddenException;
import com.bookinghub.catalog.core.exceptions.NotFoundException;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InactivateEstablishmentUseCaseTest {

    @Mock
    private EstablishmentRepository repository;

    @InjectMocks
    private InactivateEstablishmentUseCase inactivateEstablishmentUseCase;

    @Test
    void shouldInactivateWhenOwnerIsValid() {
        UUID id = UUID.randomUUID();
        String ownerId = "owner-123";
        Establishment existing = Establishment.builder()
                .id(id)
                .ownerId(ownerId)
                .active(true)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        inactivateEstablishmentUseCase.execute(id, ownerId);

        assertFalse(existing.isActive());
        verify(repository).save(existing);
    }

    @Test
    void shouldThrowForbiddenWhenNotOwner() {
        UUID id = UUID.randomUUID();
        String ownerId = "owner-123";
        Establishment existing = Establishment.builder()
                .id(id)
                .ownerId("other-owner")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenException.class, () -> inactivateEstablishmentUseCase.execute(id, ownerId));
        verify(repository, never()).save(any());
    }
}
