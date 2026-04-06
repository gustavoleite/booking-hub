package com.bookinghub.search.infrastructure.adapters.in.messaging;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.usecases.IndexEstablishmentUseCase;
import com.bookinghub.search.core.usecases.UpdateEstablishmentUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EstablishmentEventListenerTest {

    @Mock IndexEstablishmentUseCase indexUseCase;
    @Mock UpdateEstablishmentUseCase updateUseCase;
    @InjectMocks EstablishmentEventListener listener;

    @Test
    void onCreated_shouldMapAllFieldsCorrectly() {
        var event = new EstablishmentEvent("id1", "Barbearia Central", "Desc",
                "São Paulo", "SP", "01310-100",
                new BigDecimal("-23.5505"), new BigDecimal("-46.6333"));

        listener.onEstablishmentCreated(event);

        var captor = ArgumentCaptor.forClass(EstablishmentDocument.class);
        verify(indexUseCase).execute(captor.capture());
        var doc = captor.getValue();

        assertThat(doc.getId()).isEqualTo("id1");
        assertThat(doc.getName()).isEqualTo("Barbearia Central");
        assertThat(doc.getCity()).isEqualTo("São Paulo");
        assertThat(doc.getState()).isEqualTo("SP");
        assertThat(doc.getZipCode()).isEqualTo("01310-100");
        assertThat(doc.getLat()).isEqualTo(-23.5505);
        assertThat(doc.getLon()).isEqualTo(-46.6333);
        assertThat(doc.getServices()).isEmpty();
        assertThat(doc.getProfessionals()).isEmpty();
        assertThat(doc.getTotalReviews()).isZero();
    }

    @Test
    void onCreated_shouldHandleNullCoordinates() {
        var event = new EstablishmentEvent("id1", "Barbearia", null, "SP", "SP", null, null, null);

        listener.onEstablishmentCreated(event);

        var captor = ArgumentCaptor.forClass(EstablishmentDocument.class);
        verify(indexUseCase).execute(captor.capture());
        assertThat(captor.getValue().getLat()).isNull();
        assertThat(captor.getValue().getLon()).isNull();
    }

    @Test
    void onUpdated_shouldCallUpdateUseCase() {
        var event = new EstablishmentEvent("id2", "Salão Novo", "Nova Desc", "RJ", "RJ", "22000",
                new BigDecimal("-22.9"), new BigDecimal("-43.1"));

        listener.onEstablishmentUpdated(event);

        verify(updateUseCase).execute(
                "id2", "Salão Novo", "Nova Desc", "RJ", "RJ", "22000", -22.9, -43.1
        );
    }
}
