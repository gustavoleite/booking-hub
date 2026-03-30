package com.bookinghub.catalog.infrastructure.adapters.in.rest;

import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import com.bookinghub.catalog.core.usecases.UpsertProfessionalProfileUseCase;
import com.bookinghub.catalog.infrastructure.adapters.in.rest.dto.ProfessionalProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/catalog/professionals")
@RequiredArgsConstructor
@Tag(name = "2. Profissionais", description = "Gestão de Perfis de Profissionais")
public class ProfessionalController {
    private final UpsertProfessionalProfileUseCase upsertProfessionalProfileUseCase;
    private final ProfessionalRepository professionalRepository;

    @PutMapping("/me")
    @Operation(summary = "Criar/Atualizar meu perfil profissional")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Professional> upsertMyProfile(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId,
            @RequestBody ProfessionalProfileRequest request) {
        UUID userUuid = parseId(userId);
        Professional domain = Professional.builder()
                .name(request.getName())
                .bio(request.getBio())
                .avatarUrl(request.getAvatarUrl())
                .specialties(request.getSpecialties())
                .build();
        return ResponseEntity.ok(upsertProfessionalProfileUseCase.execute(userUuid, domain));
    }

    @GetMapping("/me")
    @Operation(summary = "Obter meu perfil profissional")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Professional> getMyProfile(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {
        UUID userUuid = parseId(userId);
        return professionalRepository.findById(userUuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("O X-User-Id fornecido não é um UUID válido: " + id);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professional> getProfile(@PathVariable String id) {
        try {
            return professionalRepository.findById(UUID.fromString(id))
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
