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
    void shouldAddProfessionalAndServicesWhenActive() {
        var doc = EstablishmentDocument.builder().id("est1")
                .professionals(Collections.emptyList())
                .services(Collections.emptyList())
                .build();
        when(repository.findById("est1")).thenReturn(Optional.of(doc));

        var offering = EstablishmentDocument.ServiceEntry.builder()
                .serviceId("svc1").title("Corte").minPrice(50.0).maxPrice(50.0).build();

        useCase.execute("est1", "prof1", "João", List.of("Cabeleireiro"), true, List.of(offering));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository).upsertPartial(eq("est1"), captor.capture());
        Map<String, Object> fields = captor.getValue();

        @SuppressWarnings("unchecked")
        List<EstablishmentDocument.ProfessionalEntry> profs =
                (List<EstablishmentDocument.ProfessionalEntry>) fields.get("professionals");
        assertThat(profs).hasSize(1);
        assertThat(profs.get(0).getName()).isEqualTo("João");
        assertThat(profs.get(0).getProfessionalId()).isEqualTo("prof1");
        assertThat(profs.get(0).getSpecialties()).containsExactly("Cabeleireiro");
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

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository).upsertPartial(eq("est1"), captor.capture());
        @SuppressWarnings("unchecked")
        List<EstablishmentDocument.ProfessionalEntry> profs =
                (List<EstablishmentDocument.ProfessionalEntry>) captor.getValue().get("professionals");
        assertThat(profs).isEmpty();
    }

    @Test
    void shouldReplaceExistingServiceWithSameId() {
        var existing = EstablishmentDocument.ServiceEntry.builder()
                .serviceId("svc1").title("Corte Antigo").minPrice(30.0).maxPrice(30.0).build();
        var doc = EstablishmentDocument.builder().id("est1")
                .professionals(Collections.emptyList())
                .services(List.of(existing))
                .build();
        when(repository.findById("est1")).thenReturn(Optional.of(doc));

        var updated = EstablishmentDocument.ServiceEntry.builder()
                .serviceId("svc1").title("Corte Novo").minPrice(60.0).maxPrice(60.0).build();

        useCase.execute("est1", "prof1", "João", Collections.emptyList(), true, List.of(updated));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository).upsertPartial(eq("est1"), captor.capture());
        @SuppressWarnings("unchecked")
        List<EstablishmentDocument.ServiceEntry> services =
                (List<EstablishmentDocument.ServiceEntry>) captor.getValue().get("services");
        assertThat(services).hasSize(1);
        assertThat(services.get(0).getTitle()).isEqualTo("Corte Novo");
        assertThat(services.get(0).getMinPrice()).isEqualTo(60.0);
    }

    @Test
    void shouldCalculateMinAndMaxPriceFromMultipleServices() {
        var doc = EstablishmentDocument.builder().id("est1")
                .professionals(Collections.emptyList())
                .services(Collections.emptyList())
                .build();
        when(repository.findById("est1")).thenReturn(Optional.of(doc));

        var cheap = EstablishmentDocument.ServiceEntry.builder()
                .serviceId("s1").title("Corte").minPrice(40.0).maxPrice(40.0).build();
        var expensive = EstablishmentDocument.ServiceEntry.builder()
                .serviceId("s2").title("Coloração").minPrice(120.0).maxPrice(120.0).build();

        useCase.execute("est1", "prof1", "João", Collections.emptyList(), true, List.of(cheap, expensive));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository).upsertPartial(eq("est1"), captor.capture());
        assertThat(captor.getValue().get("minPrice")).isEqualTo(40.0);
        assertThat(captor.getValue().get("maxPrice")).isEqualTo(120.0);
    }

    @Test
    void shouldSetNullPricesWhenNoServiceHasPrice() {
        var doc = EstablishmentDocument.builder().id("est1")
                .professionals(Collections.emptyList())
                .services(Collections.emptyList())
                .build();
        when(repository.findById("est1")).thenReturn(Optional.of(doc));

        var noPrice = EstablishmentDocument.ServiceEntry.builder()
                .serviceId("s1").title("Consulta")
                .minPrice(null).maxPrice(null).build();

        useCase.execute("est1", "prof1", "João", Collections.emptyList(), true, List.of(noPrice));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository).upsertPartial(eq("est1"), captor.capture());
        assertThat(captor.getValue().get("minPrice")).isNull();
        assertThat(captor.getValue().get("maxPrice")).isNull();
    }

    @Test
    void shouldSetNullPricesWhenServiceListIsEmpty() {
        var doc = EstablishmentDocument.builder().id("est1")
                .professionals(Collections.emptyList())
                .services(Collections.emptyList())
                .build();
        when(repository.findById("est1")).thenReturn(Optional.of(doc));

        useCase.execute("est1", "prof1", "João", Collections.emptyList(), false, Collections.emptyList());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository).upsertPartial(eq("est1"), captor.capture());
        assertThat(captor.getValue().get("minPrice")).isNull();
        assertThat(captor.getValue().get("maxPrice")).isNull();
    }

    @Test
    void shouldHandleNullProfessionalsListInDocument() {
        var doc = EstablishmentDocument.builder().id("est1")
                .professionals(null)
                .services(null)
                .build();
        when(repository.findById("est1")).thenReturn(Optional.of(doc));

        useCase.execute("est1", "prof1", "João", Collections.emptyList(), true, Collections.emptyList());

        verify(repository).upsertPartial(eq("est1"), any());
    }
}
