package com.bookinghub.catalog.core.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.exceptions.ForbiddenException;
import com.bookinghub.catalog.core.exceptions.NotFoundException;
import com.bookinghub.catalog.core.ports.CatalogEventPublisher;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateEstablishmentUseCaseTest {

    @Mock
    private EstablishmentRepository repository;

    @Mock
    private CatalogEventPublisher eventPublisher;

    @InjectMocks
    private UpdateEstablishmentUseCase updateEstablishmentUseCase;

    @Test
    void shouldUpdateWhenOwnerIsValid() {
        UUID id = UUID.randomUUID();
        String ownerId = "owner-123";
        Establishment existing = Establishment.builder()
                .id(id)
                .ownerId(ownerId)
                .name("Old Name")
                .build();
        Establishment updateData = Establishment.builder()
                .name("New Name")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        updateEstablishmentUseCase.execute(id, ownerId, updateData);

        verify(repository).save(any());
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

        assertThrows(ForbiddenException.class, () ->
                updateEstablishmentUseCase.execute(id, ownerId, Establishment.builder().build())
        );
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenEstablishmentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                updateEstablishmentUseCase.execute(id, "any", Establishment.builder().build())
        );
    }

    @Test
    void shouldPublishEstablishmentUpdatedEvent() {
        UUID id = UUID.randomUUID();
        String ownerId = "owner-123";
        Establishment existing = Establishment.builder()
                .id(id)
                .ownerId(ownerId)
                .name("Old Name")
                .build();
        Establishment updateData = Establishment.builder()
                .name("New Name")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        updateEstablishmentUseCase.execute(id, ownerId, updateData);

        verify(eventPublisher).publishEstablishmentUpdated(any());
    }
}
