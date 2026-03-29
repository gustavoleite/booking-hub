package com.bookinghub.catalog.infrastructure.adapters.in.grpc;

import br.com.beauty.catalog.grpc.*;
import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.domain.WorkSchedule;
import com.bookinghub.catalog.core.usecases.GetProfessionalScheduleUseCase;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class CatalogGrpcServiceAdapter extends CatalogGrpcServiceGrpc.CatalogGrpcServiceImplBase {
    private final GetProfessionalScheduleUseCase getProfessionalScheduleUseCase;

    @Override
    public void getProfessionalSchedule(ScheduleRequest request, StreamObserver<ScheduleResponse> responseObserver) {
        try {
            Affiliation affiliation = getProfessionalScheduleUseCase.execute(
                    UUID.fromString(request.getEstablishmentId()),
                    UUID.fromString(request.getProfessionalId())
            );

            // Find specific service offering for the duration and price
            var offering = affiliation.getServiceOfferings().stream()
                    .filter(so -> so.getProvidedServiceId().equals(UUID.fromString(request.getServiceId())))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Service offering not found for this professional"));

            ScheduleResponse response = ScheduleResponse.newBuilder()
                    .setIsActive(affiliation.isActive())
                    .setPrice(offering.getPrice().doubleValue())
                    .setDurationMinutes(offering.getDurationMinutes())
                    .addAllFixedSchedule(affiliation.getWorkSchedules().stream()
                            .map(this::mapDaySchedule)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    private DaySchedule mapDaySchedule(WorkSchedule ws) {
        return DaySchedule.newBuilder()
                .setDayOfWeek(ws.getDayOfWeek())
                .setStartTime(ws.getStartTime().toString())
                .setEndTime(ws.getEndTime().toString())
                .build();
    }
}
