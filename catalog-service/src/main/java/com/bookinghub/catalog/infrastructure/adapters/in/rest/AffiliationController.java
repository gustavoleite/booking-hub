package com.bookinghub.catalog.infrastructure.adapters.in.rest;

import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.usecases.AddProfessionalToEstablishmentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/catalog/establishments/{establishmentId}/affiliations")
@RequiredArgsConstructor
@Tag(name = "3. Afiliações", description = "Vincular Profissionais a Estabelecimentos")
public class AffiliationController {
    private final AddProfessionalToEstablishmentUseCase addProfessionalToEstablishmentUseCase;

    @PostMapping
    @Operation(summary = "Adicionar profissional ao estabelecimento")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Affiliation> addProfessional(
            @PathVariable UUID establishmentId,
            @RequestParam UUID professionalId,
            @RequestBody Affiliation affiliation) {
        Affiliation saved = addProfessionalToEstablishmentUseCase.execute(establishmentId, professionalId, affiliation);
        return ResponseEntity.ok(saved);
    }
}
