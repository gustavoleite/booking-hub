package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.exceptions.ConflictException;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProfessionalProfileUseCaseTest {

    @Mock
    private ProfessionalRepository repository;

    @InjectMocks
    private CreateProfessionalProfileUseCase createProfessionalProfileUseCase;

    @Test
    void shouldCreateNewProfile() {
        UUID id = UUID.randomUUID();
        Professional data = Professional.builder().name("John Doe").build();

        when(repository.findById(id)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Professional result = createProfessionalProfileUseCase.execute(id, data);

        assertEquals(id, result.getId());
        assertEquals("John Doe", result.getName());
        verify(repository).save(any());
    }

    @Test
    void shouldThrowConflictWhenProfileAlreadyExists() {
        UUID id = UUID.randomUUID();
        Professional existing = Professional.builder().id(id).name("Existing").build();
        Professional data = Professional.builder().name("John Doe").build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class, () -> createProfessionalProfileUseCase.execute(id, data));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        UUID id = UUID.randomUUID();
        Professional data = Professional.builder().name("").build();

        assertThrows(BusinessRuleException.class, () -> createProfessionalProfileUseCase.execute(id, data));
        verify(repository, never()).save(any());
    }
}
