package com.bookinghub.search.core.usecases;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexAffiliationUseCaseTest {

    @Mock EstablishmentSearchRepository repository;
    @InjectMocks IndexAffiliationUseCase useCase;

    @Test
    void shouldAddProfessionalAndServicesToExistingDocument() {
        var doc = EstablishmentDocument.builder().id("est1")
                .professionals(Collections.emptyList())
                .services(Collections.emptyList())
                .build();
        when(repository.findById("est1")).thenReturn(Optional.of(doc));

        var offering = EstablishmentDocument.ServiceEntry.builder()
                .serviceId("svc1").title("Corte").minPrice(50.0).maxPrice(50.0).build();

        useCase.execute("est1", "prof1", "João", List.of("Cabeleireiro"), true, List.of(offering));

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository).upsertPartial(eq("est1"), captor.capture());
        Map<String, Object> fields = captor.getValue();

        @SuppressWarnings("unchecked")
        List<EstablishmentDocument.ProfessionalEntry> profs =
                (List<EstablishmentDocument.ProfessionalEntry>) fields.get("professionals");
        assertThat(profs).hasSize(1);
        assertThat(profs.get(0).getName()).isEqualTo("João");
    }

    @Test
    void shouldSkipWhenEstablishmentNotIndexed() {
        when(repository.findById("est1")).thenReturn(Optional.empty());
        useCase.execute("est1", "prof1", "João", Collections.emptyList(), true, Collections.emptyList());
        verify(repository, never()).upsertPartial(anyString(), any());
    }

    @Test
    void shouldRemoveProfessionalWhenInactive() {
        var existingProf = EstablishmentDocument.ProfessionalEntry.builder()
                .professionalId("prof1").name("João").specialties(Collections.emptyList()).build();
        var doc = EstablishmentDocument.builder().id("est1")
                .professionals(List.of(existingProf))
                .services(Collections.emptyList())
                .build();
        when(repository.findById("est1")).thenReturn(Optional.of(doc));

        useCase.execute("est1", "prof1", "João", Collections.emptyList(), false, Collections.emptyList());

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository).upsertPartial(eq("est1"), captor.capture());
        @SuppressWarnings("unchecked")
        List<EstablishmentDocument.ProfessionalEntry> profs =
                (List<EstablishmentDocument.ProfessionalEntry>) captor.getValue().get("professionals");
        assertThat(profs).isEmpty();
    }
}
