package com.bookinghub.search.infrastructure.adapters.in.messaging;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.usecases.IndexAffiliationUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AffiliationEventListenerTest {

    @Mock IndexAffiliationUseCase useCase;
    @InjectMocks AffiliationEventListener listener;

    @Test
    void onCreated_shouldMapServiceOfferingPriceToMinAndMaxPrice() {
        var offering = new AffiliationEvent.ServiceOfferingEvent("svc1", "Corte Masculino",
                new BigDecimal("120.00"), 60);
        var event = new AffiliationEvent("aff1", "est1", "prof1", "João Barbeiro",
                List.of("Barbeiro"), true, List.of(offering));

        listener.onAffiliationCreated(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EstablishmentDocument.ServiceEntry>> captor = ArgumentCaptor.forClass(List.class);
        verify(useCase).execute(eq("est1"), eq("prof1"), eq("João Barbeiro"),
                eq(List.of("Barbeiro")), eq(true), captor.capture());

        var serviceEntry = captor.getValue().get(0);
        assertThat(serviceEntry.getServiceId()).isEqualTo("svc1");
        assertThat(serviceEntry.getTitle()).isEqualTo("Corte Masculino");
        assertThat(serviceEntry.getMinPrice()).isEqualTo(120.0);
        assertThat(serviceEntry.getMaxPrice()).isEqualTo(120.0);
    }

    @Test
    void onCreated_shouldHandleNullPriceInOffering() {
        var offering = new AffiliationEvent.ServiceOfferingEvent("svc1", "Consulta", null, 30);
        var event = new AffiliationEvent("aff1", "est1", "prof1", "Maria",
                Collections.emptyList(), true, List.of(offering));

        listener.onAffiliationCreated(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EstablishmentDocument.ServiceEntry>> captor = ArgumentCaptor.forClass(List.class);
        verify(useCase).execute(any(), any(), any(), any(), anyBoolean(), captor.capture());

        assertThat(captor.getValue().get(0).getMinPrice()).isNull();
        assertThat(captor.getValue().get(0).getMaxPrice()).isNull();
    }

    @Test
    void onCreated_shouldHandleNullServiceOfferings() {
        var event = new AffiliationEvent("aff1", "est1", "prof1", "João",
                null, true, null);

        listener.onAffiliationCreated(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EstablishmentDocument.ServiceEntry>> serviceCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> specialtiesCaptor = ArgumentCaptor.forClass(List.class);
        verify(useCase).execute(eq("est1"), eq("prof1"), eq("João"),
                specialtiesCaptor.capture(), eq(true), serviceCaptor.capture());

        assertThat(serviceCaptor.getValue()).isEmpty();
        assertThat(specialtiesCaptor.getValue()).isEmpty();
    }

    @Test
    void onUpdated_shouldDelegateToSameProcessingLogic() {
        var offering = new AffiliationEvent.ServiceOfferingEvent("svc1", "Barba",
                new BigDecimal("50.00"), 30);
        var event = new AffiliationEvent("aff1", "est1", "prof1", "Pedro",
                List.of("Barbeiro"), false, List.of(offering));

        listener.onAffiliationUpdated(event);

        verify(useCase).execute(eq("est1"), eq("prof1"), eq("Pedro"),
                eq(List.of("Barbeiro")), eq(false), any());
    }
}
