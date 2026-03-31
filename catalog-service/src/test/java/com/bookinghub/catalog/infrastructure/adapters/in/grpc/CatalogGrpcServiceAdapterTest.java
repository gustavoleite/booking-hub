package com.bookinghub.catalog.infrastructure.adapters.in.grpc;

import br.com.beauty.catalog.grpc.ScheduleRequest;
import br.com.beauty.catalog.grpc.ScheduleResponse;
import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.domain.ServiceOffering;
import com.bookinghub.catalog.core.domain.WorkSchedule;
import com.bookinghub.catalog.core.usecases.GetProfessionalScheduleUseCase;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogGrpcServiceAdapterTest {

    @Mock
    private GetProfessionalScheduleUseCase getProfessionalScheduleUseCase;

    @Mock
    private StreamObserver<ScheduleResponse> responseObserver;

    @InjectMocks
    private CatalogGrpcServiceAdapter adapter;

    @Test
    void shouldGetProfessionalSchedule() {
        UUID estId = UUID.randomUUID();
        UUID profId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        
        ScheduleRequest request = ScheduleRequest.newBuilder()
                .setEstablishmentId(estId.toString())
                .setProfessionalId(profId.toString())
                .setServiceId(serviceId.toString())
                .build();

        ServiceOffering offering = ServiceOffering.builder()
                .providedServiceId(serviceId)
                .price(new BigDecimal("100.00"))
                .durationMinutes(30)
                .build();

        WorkSchedule schedule = WorkSchedule.builder()
                .dayOfWeek(1)
                .startTime(LocalTime.of(8,0))
                .endTime(LocalTime.of(12,0))
                .build();

        Affiliation affiliation = Affiliation.builder()
                .active(true)
                .serviceOfferings(List.of(offering))
                .workSchedules(List.of(schedule))
                .build();

        when(getProfessionalScheduleUseCase.execute(estId, profId)).thenReturn(affiliation);

        adapter.getProfessionalSchedule(request, responseObserver);

        ArgumentCaptor<ScheduleResponse> responseCaptor = ArgumentCaptor.forClass(ScheduleResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ScheduleResponse response = responseCaptor.getValue();
        assertTrue(response.getIsActive());
        assertEquals(100.0, response.getPrice());
        assertEquals(30, response.getDurationMinutes());
        assertEquals(1, response.getFixedScheduleCount());
        assertEquals(1, response.getFixedSchedule(0).getDayOfWeek());
    }

    @Test
    void shouldReturnErrorWhenAffiliationNotFound() {
        UUID estId = UUID.randomUUID();
        UUID profId = UUID.randomUUID();
        
        ScheduleRequest request = ScheduleRequest.newBuilder()
                .setEstablishmentId(estId.toString())
                .setProfessionalId(profId.toString())
                .build();

        when(getProfessionalScheduleUseCase.execute(any(), any())).thenThrow(new RuntimeException("Not found"));

        adapter.getProfessionalSchedule(request, responseObserver);

        verify(responseObserver).onError(any());
        verify(responseObserver, never()).onNext(any());
    }

    @Test
    void shouldReturnErrorWhenServiceOfferingNotFound() {
        UUID estId = UUID.randomUUID();
        UUID profId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        
        ScheduleRequest request = ScheduleRequest.newBuilder()
                .setEstablishmentId(estId.toString())
                .setProfessionalId(profId.toString())
                .setServiceId(serviceId.toString())
                .build();

        Affiliation affiliation = Affiliation.builder()
                .serviceOfferings(List.of()) // No offerings
                .build();

        when(getProfessionalScheduleUseCase.execute(any(), any())).thenReturn(affiliation);

        adapter.getProfessionalSchedule(request, responseObserver);

        verify(responseObserver).onError(any());
    }
}
