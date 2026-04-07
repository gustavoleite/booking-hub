package com.bookinghub.catalog.core.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListMyEstablishmentsUseCaseTest {

    @Mock
    private EstablishmentRepository establishmentRepository;

    @InjectMocks
    private ListMyEstablishmentsUseCase useCase;

    @Test
    void shouldListEstablishments() {
        String ownerId = "owner-1";
        List<Establishment> establishments = List.of(Establishment.builder().ownerId(ownerId).build());
        when(establishmentRepository.findByOwnerId(ownerId)).thenReturn(establishments);

        List<Establishment> result = useCase.execute(ownerId);

        assertEquals(1, result.size());
        assertEquals(ownerId, result.get(0).getOwnerId());
    }
}
