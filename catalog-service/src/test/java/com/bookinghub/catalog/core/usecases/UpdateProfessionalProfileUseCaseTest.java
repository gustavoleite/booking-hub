package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.exceptions.ProfessionalNotFoundException;
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
class UpdateProfessionalProfileUseCaseTest {

    @Mock
    private ProfessionalRepository repository;

    @InjectMocks
    private UpdateProfessionalProfileUseCase updateProfessionalProfileUseCase;

    @Test
    void shouldUpdateExistingProfile() {
        UUID id = UUID.randomUUID();
        Professional existing = Professional.builder().id(id).name("Old Name").build();
        Professional updateData = Professional.builder().name("New Name").bio("New Bio").build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        Professional result = updateProfessionalProfileUseCase.execute(id, updateData);

        assertEquals("New Name", result.getName());
        assertEquals("New Bio", result.getBio());
        verify(repository).save(existing);
    }

    @Test
    void shouldThrowNotFoundWhenProfileDoesNotExist() {
        UUID id = UUID.randomUUID();
        Professional updateData = Professional.builder().name("New Name").build();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProfessionalNotFoundException.class, () -> updateProfessionalProfileUseCase.execute(id, updateData));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        UUID id = UUID.randomUUID();
        Professional data = Professional.builder().name("").build();

        assertThrows(BusinessRuleException.class, () -> updateProfessionalProfileUseCase.execute(id, data));
        verify(repository, never()).save(any());
    }
}
