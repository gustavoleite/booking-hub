package com.bookinghub.catalog.infrastructure.adapters.out.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.catalog.core.domain.Address;
import com.bookinghub.catalog.core.domain.Establishment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgresEstablishmentRepositoryAdapterTest {

    @Mock
    private JpaEstablishmentRepository jpaRepository;

    @InjectMocks
    private PostgresEstablishmentRepositoryAdapter adapter;

    @Test
    void shouldSaveNewEstablishment() {
        UUID id = UUID.randomUUID();
        Establishment domain = Establishment.builder()
                .id(id)
                .name("Salon")
                .address(Address.builder().street("Street").build())
                .build();

        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        EstablishmentEntity entity = new EstablishmentEntity();
        entity.setId(id);
        entity.setName("Salon");
        entity.setAddress(new AddressEmbeddable());
        when(jpaRepository.save(any(EstablishmentEntity.class))).thenReturn(entity);

        Establishment result = adapter.save(domain);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(jpaRepository).save(any(EstablishmentEntity.class));
    }

    @Test
    void shouldUpdateExistingEstablishment() {
        UUID id = UUID.randomUUID();
        Establishment domain = Establishment.builder()
                .id(id)
                .name("Updated Name")
                .address(Address.builder().street("Street").build())
                .build();

        EstablishmentEntity existingEntity = EstablishmentEntity.builder()
                .id(id)
                .name("Old Name")
                .defaultBusinessHours(new ArrayList<>())
                .providedServices(new ArrayList<>())
                .build();

        when(jpaRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(jpaRepository.save(any(EstablishmentEntity.class))).thenReturn(existingEntity);

        Establishment result = adapter.save(domain);

        assertNotNull(result);
        assertEquals("Updated Name", existingEntity.getName());
        verify(jpaRepository).save(existingEntity);
    }

    @Test
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        EstablishmentEntity entity = EstablishmentEntity.builder()
                .id(id)
                .address(new AddressEmbeddable())
                .build();
        when(jpaRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(entity));

        Optional<Establishment> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void shouldFindByOwnerId() {
        String ownerId = "owner-1";
        EstablishmentEntity entity = EstablishmentEntity.builder()
                .ownerId(ownerId)
                .address(new AddressEmbeddable())
                .build();
        when(jpaRepository.findByOwnerIdAndActiveTrue(ownerId)).thenReturn(List.of(entity));

        List<Establishment> result = adapter.findByOwnerId(ownerId);

        assertEquals(1, result.size());
        assertEquals(ownerId, result.get(0).getOwnerId());
    }

    @Test
    void shouldCheckIfExistsByCnpj() {
        String cnpj = "123";
        when(jpaRepository.existsByCnpj(cnpj)).thenReturn(true);
        assertTrue(adapter.existsByCnpj(cnpj));
    }
}
