package com.bookinghub.catalog.infrastructure.adapters.in.rest;

import com.bookinghub.catalog.core.domain.Address;
import com.bookinghub.catalog.core.domain.BusinessHour;
import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.domain.ProvidedService;
import com.bookinghub.catalog.core.usecases.*;
import com.bookinghub.catalog.infrastructure.adapters.in.rest.dto.EstablishmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/catalog/establishments")
@RequiredArgsConstructor
public class EstablishmentController {
    private final CreateEstablishmentUseCase createEstablishmentUseCase;
    private final UpdateEstablishmentUseCase updateEstablishmentUseCase;
    private final InactivateEstablishmentUseCase inactivateEstablishmentUseCase;
    private final GetEstablishmentDetailsUseCase getEstablishmentDetailsUseCase;
    private final ListMyEstablishmentsUseCase listMyEstablishmentsUseCase;
    private final AddProvidedServiceUseCase addProvidedServiceUseCase;

    @PostMapping
    public ResponseEntity<Establishment> create(
            @RequestHeader("X-User-Id") String ownerId,
            @RequestBody EstablishmentRequest request) {
        Establishment domain = toDomain(request, ownerId);
        Establishment saved = createEstablishmentUseCase.execute(domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/my-salons")
    public ResponseEntity<List<Establishment>> listMySalons(@RequestHeader("X-User-Id") String ownerId) {
        return ResponseEntity.ok(listMyEstablishmentsUseCase.execute(ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Establishment> getDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(getEstablishmentDetailsUseCase.execute(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Establishment> update(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String ownerId,
            @RequestBody EstablishmentRequest request) {
        Establishment domain = toDomain(request, ownerId);
        return ResponseEntity.ok(updateEstablishmentUseCase.execute(id, ownerId, domain));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String ownerId) {
        inactivateEstablishmentUseCase.execute(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/services")
    public ResponseEntity<ProvidedService> addService(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String ownerId,
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
                .address(Address.builder()
                        .street(request.getAddress().getStreet())
                        .number(request.getAddress().getNumber())
                        .zipCode(request.getAddress().getZipCode())
                        // City and State are not in our domain yet but are in DTO/DB. 
                        // I'll keep it simple for now as per domain model.
                        .build())
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
