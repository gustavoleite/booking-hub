package com.bookinghub.catalog.infrastructure.adapters.in.rest;

import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import com.bookinghub.catalog.core.usecases.UpsertProfessionalProfileUseCase;
import com.bookinghub.catalog.infrastructure.adapters.in.rest.dto.ProfessionalProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/catalog/professionals")
@RequiredArgsConstructor
public class ProfessionalController {
    private final UpsertProfessionalProfileUseCase upsertProfessionalProfileUseCase;
    private final ProfessionalRepository professionalRepository;

    @PutMapping("/me")
    public ResponseEntity<Professional> upsertMyProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody ProfessionalProfileRequest request) {
        Professional domain = Professional.builder()
                .name(request.getName())
                .bio(request.getBio())
                .avatarUrl(request.getAvatarUrl())
                .specialties(request.getSpecialties())
                .build();
        return ResponseEntity.ok(upsertProfessionalProfileUseCase.execute(UUID.fromString(userId), domain));
    }

    @GetMapping("/me")
    public ResponseEntity<Professional> getMyProfile(@RequestHeader("X-User-Id") String userId) {
        return professionalRepository.findById(UUID.fromString(userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
