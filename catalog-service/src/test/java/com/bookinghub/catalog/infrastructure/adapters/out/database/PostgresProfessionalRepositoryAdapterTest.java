package com.bookinghub.catalog.infrastructure.adapters.out.database;

import com.bookinghub.catalog.core.domain.Professional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostgresProfessionalRepositoryAdapterTest {

    @Mock
    private JpaProfessionalRepository jpaRepository;

    @InjectMocks
    private PostgresProfessionalRepositoryAdapter adapter;

    @Test
    void shouldSaveProfessional() {
        UUID id = UUID.randomUUID();
        Professional domain = Professional.builder().id(id).name("Pro").build();
        ProfessionalEntity entity = ProfessionalEntity.builder().id(id).name("Pro").build();
        
        when(jpaRepository.save(any(ProfessionalEntity.class))).thenReturn(entity);

        Professional result = adapter.save(domain);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        ProfessionalEntity entity = ProfessionalEntity.builder().id(id).build();
        when(jpaRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(entity));

        Optional<Professional> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }
}
