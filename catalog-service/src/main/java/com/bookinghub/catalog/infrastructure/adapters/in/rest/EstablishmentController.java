package com.bookinghub.catalog.infrastructure.adapters.in.rest;

import com.bookinghub.catalog.core.domain.Address;
import com.bookinghub.catalog.core.domain.BusinessHour;
import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.domain.ProvidedService;
import com.bookinghub.catalog.core.usecases.*;
import com.bookinghub.catalog.infrastructure.adapters.in.rest.dto.EstablishmentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/establishments")
@RequiredArgsConstructor
@Tag(name = "1. Estabelecimentos", description = "Gestão de Salões (Requer ROLE_OWNER)")
public class EstablishmentController {
    private final CreateEstablishmentUseCase createEstablishmentUseCase;
    private final UpdateEstablishmentUseCase updateEstablishmentUseCase;
    private final InactivateEstablishmentUseCase inactivateEstablishmentUseCase;
    private final GetEstablishmentDetailsUseCase getEstablishmentDetailsUseCase;
    private final ListMyEstablishmentsUseCase listMyEstablishmentsUseCase;
    private final AddProvidedServiceUseCase addProvidedServiceUseCase;

    @PostMapping
    @Operation(summary = "Criar um novo estabelecimento")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Establishment> create(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String ownerId,
            @RequestBody EstablishmentRequest request) {
        Establishment domain = toDomain(request, ownerId);
        Establishment saved = createEstablishmentUseCase.execute(domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/my-salons")
    @Operation(summary = "Listar meus estabelecimentos")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<Establishment>> listMySalons(@Parameter(hidden = true) @RequestHeader("X-User-Id") String ownerId) {
        return ResponseEntity.ok(listMyEstablishmentsUseCase.execute(ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Establishment> getDetails(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(getEstablishmentDetailsUseCase.execute(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um estabelecimento")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Establishment> update(
            @PathVariable("id") UUID id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String ownerId,
            @RequestBody EstablishmentRequest request) {
        Establishment domain = toDomain(request, ownerId);
        return ResponseEntity.ok(updateEstablishmentUseCase.execute(id, ownerId, domain));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar um estabelecimento")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(
            @PathVariable("id") UUID id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String ownerId) {
        inactivateEstablishmentUseCase.execute(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/services")
    @Operation(summary = "Adicionar serviço prestado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ProvidedService> addService(
            @PathVariable("id") UUID id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String ownerId,
            @RequestBody EstablishmentRequest.ProvidedServiceDto request) {
        ProvidedService service = ProvidedService.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        return ResponseEntity.ok(addProvidedServiceUseCase.execute(id, ownerId, service));
    }

    private Establishment toDomain(EstablishmentRequest request, String ownerId) {
        return Establishment.builder()
                .ownerId(ownerId)
                .name(request.getName())
                .cnpj(request.getCnpj())
                .description(request.getDescription())
                .address(request.getAddress() != null ? Address.builder()
                        .street(request.getAddress().getStreet())
                        .number(request.getAddress().getNumber())
                        .city(request.getAddress().getCity())
                        .state(request.getAddress().getState())
                        .zipCode(request.getAddress().getZipCode())
                        .latitude(request.getAddress().getLatitude())
                        .longitude(request.getAddress().getLongitude())
                        .build() : null)
                .defaultBusinessHours(request.getBusinessHours() != null ? request.getBusinessHours().stream()
                        .map(bh -> BusinessHour.builder()
                                .dayOfWeek(bh.getDayOfWeek())
                                .openTime(bh.getOpenTime())
                                .closeTime(bh.getCloseTime())
                                .build())
                        .collect(Collectors.toList()) : null)
                .providedServices(request.getServices() != null ? request.getServices().stream()
                        .map(ps -> ProvidedService.builder()
                                .title(ps.getTitle())
                                .description(ps.getDescription())
                                .build())
                        .collect(Collectors.toList()) : null)
                .build();
    }
}
