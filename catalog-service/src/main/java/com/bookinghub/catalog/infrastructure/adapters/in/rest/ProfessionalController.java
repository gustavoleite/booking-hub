package com.bookinghub.catalog.infrastructure.adapters.in.rest;

import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.exceptions.ProfessionalNotFoundException;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import com.bookinghub.catalog.core.usecases.CreateProfessionalProfileUseCase;
import com.bookinghub.catalog.core.usecases.UpdateProfessionalProfileUseCase;
import com.bookinghub.catalog.infrastructure.adapters.in.rest.dto.ProfessionalProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
@Tag(name = "2. Profissionais", description = "Gestão de Perfis de Profissionais")
public class ProfessionalController {
  private final CreateProfessionalProfileUseCase createProfessionalProfileUseCase;
  private final UpdateProfessionalProfileUseCase updateProfessionalProfileUseCase;
  private final ProfessionalRepository professionalRepository;

  @PostMapping("/me")
  @Operation(summary = "Criar meu perfil profissional")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<Professional> createMyProfile(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId,
            @RequestBody ProfessionalProfileRequest request) {
    UUID userUuid = parseId(userId);
    Professional domain = Professional.builder()
        .name(request.getName())
        .bio(request.getBio())
        .avatarUrl(request.getAvatarUrl())
        .specialties(request.getSpecialties())
        .build();
    return ResponseEntity.status(201)
        .body(createProfessionalProfileUseCase.execute(userUuid, domain));
  }

  @PutMapping("/me")
  @Operation(summary = "Atualizar meu perfil profissional")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<Professional> updateMyProfile(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId,
            @RequestBody ProfessionalProfileRequest request) {
    UUID userUuid = parseId(userId);
    Professional domain = Professional.builder()
        .name(request.getName())
        .bio(request.getBio())
        .avatarUrl(request.getAvatarUrl())
        .specialties(request.getSpecialties())
        .build();
    return ResponseEntity.ok(updateProfessionalProfileUseCase.execute(userUuid, domain));
  }

  @GetMapping("/me")
  @Operation(summary = "Obter meu perfil profissional")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<Professional> getMyProfile(
      @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {
    UUID userUuid = parseId(userId);
    return professionalRepository.findById(userUuid)
        .map(ResponseEntity::ok)
        .orElseThrow(
            () -> new ProfessionalNotFoundException(
                "Perfil profissional não encontrado para o usuário logado."));
  }

  private UUID parseId(String id) {
    try {
      return UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      throw new BusinessRuleException("O X-User-Id fornecido não é um UUID válido: " + id, e);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Professional> getProfile(@PathVariable("id") String id) {
    try {
      return professionalRepository.findById(UUID.fromString(id))
          .map(ResponseEntity::ok)
          .orElseThrow(
              () -> new ProfessionalNotFoundException(
                  "Perfil profissional não encontrado para o ID: " + id));
    } catch (IllegalArgumentException e) {
      throw new ProfessionalNotFoundException("ID fornecido não é um UUID válido: " + id, e);
    }
  }
}
