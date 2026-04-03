package com.bookinghub.catalog.infrastructure.adapters.in.rest;

import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.domain.ServiceOffering;
import com.bookinghub.catalog.core.domain.WorkSchedule;
import com.bookinghub.catalog.core.exceptions.NotFoundException;
import com.bookinghub.catalog.core.usecases.AddProfessionalToEstablishmentUseCase;
import com.bookinghub.catalog.core.usecases.GetProfessionalScheduleUseCase;
import com.bookinghub.catalog.infrastructure.adapters.in.rest.dto.AffiliationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/establishments/{establishmentId}/affiliations")
@RequiredArgsConstructor
@Tag(name = "3. Afiliações", description = "Vincular Profissionais a Estabelecimentos")
public class AffiliationController {
    private final AddProfessionalToEstablishmentUseCase addProfessionalToEstablishmentUseCase;
    private final GetProfessionalScheduleUseCase getProfessionalScheduleUseCase;

    @PostMapping
    @Operation(summary = "Adicionar profissional ao estabelecimento")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Affiliation> addProfessional(
            @PathVariable("establishmentId") UUID establishmentId,
            @RequestParam("professionalId") UUID professionalId,
            @RequestBody AffiliationRequest request) {
        Affiliation affiliation = Affiliation.builder()
                .establishmentId(establishmentId)
                .professionalId(professionalId)
                .active(request.isActive())
                .workSchedules(request.getWorkSchedules() != null
                        ? request.getWorkSchedules().stream()
                                .map(ws -> WorkSchedule.builder()
                                        .dayOfWeek(ws.getDayOfWeek())
                                        .startTime(ws.getStartTime())
                                        .endTime(ws.getEndTime())
                                        .build())
                                .collect(Collectors.toList())
                        : Collections.emptyList())
                .serviceOfferings(request.getServiceOfferings() != null
                        ? request.getServiceOfferings().stream()
                                .map(so -> ServiceOffering.builder()
                                        .providedServiceId(so.getProvidedServiceId())
                                        .price(so.getPrice())
                                        .durationMinutes(so.getDurationMinutes())
                                        .build())
                                .collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
        Affiliation saved = addProfessionalToEstablishmentUseCase.execute(establishmentId, professionalId, affiliation);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/professional/{professionalId}/schedule")
    @Operation(summary = "Obter agenda e detalhes do serviço do profissional no estabelecimento")
    public ResponseEntity<ScheduleResponse> getProfessionalSchedule(
            @PathVariable("establishmentId") UUID establishmentId,
            @PathVariable("professionalId") UUID professionalId,
            @RequestParam("serviceId") UUID serviceId) {

        Affiliation affiliation = getProfessionalScheduleUseCase.execute(establishmentId, professionalId);

        ServiceOffering offering = affiliation.getServiceOfferings().stream()
                .filter(so -> so.getProvidedServiceId().equals(serviceId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Serviço não oferecido por este profissional neste estabelecimento"));

        ScheduleResponse response = ScheduleResponse.builder()
                .isActive(affiliation.isActive())
                .price(offering.getPrice())
                .durationMinutes(offering.getDurationMinutes())
                .fixedSchedule(affiliation.getWorkSchedules().stream()
                        .map(ws -> DaySchedule.builder()
                                .dayOfWeek(ws.getDayOfWeek())
                                .startTime(ws.getStartTime().toString())
                                .endTime(ws.getEndTime().toString())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        return ResponseEntity.ok(response);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleResponse {
        private boolean isActive;
        private BigDecimal price;
        private int durationMinutes;
        private List<DaySchedule> fixedSchedule;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DaySchedule {
        private int dayOfWeek;
        private String startTime;
        private String endTime;
    }
}
