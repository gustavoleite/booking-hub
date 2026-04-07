package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.ports.AffiliationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProfessionalScheduleUseCaseTest {

    @Mock
    private AffiliationRepository affiliationRepository;

    @InjectMocks
    private GetProfessionalScheduleUseCase useCase;

    @Test
    void shouldReturnSchedule() {
        UUID estId = UUID.randomUUID();
        UUID profId = UUID.randomUUID();
        Affiliation affiliation = Affiliation.builder().establishmentId(estId).professionalId(profId).build();
        when(affiliationRepository.findByEstablishmentIdAndProfessionalId(estId, profId)).thenReturn(Optional.of(affiliation));

        Affiliation result = useCase.execute(estId, profId);

        assertNotNull(result);
        assertEquals(estId, result.getEstablishmentId());
        assertEquals(profId, result.getProfessionalId());
    }

    @Test
    void shouldThrowExceptionWhenNotFound() {
        UUID estId = UUID.randomUUID();
        UUID profId = UUID.randomUUID();
        when(affiliationRepository.findByEstablishmentIdAndProfessionalId(estId, profId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> useCase.execute(estId, profId));
    }
}
