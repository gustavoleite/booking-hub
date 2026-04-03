package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.exceptions.ConflictException;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CreateProfessionalProfileUseCase {
    private final ProfessionalRepository professionalRepository;

    public Professional execute(UUID id, Professional professionalData) {
        validate(professionalData);
        
        if (professionalRepository.findById(id).isPresent()) {
            throw new ConflictException("Perfil profissional já existe para este usuário.");
        }

        Professional newProfile = Professional.builder()
                .id(id)
                .name(professionalData.getName())
                .bio(professionalData.getBio())
                .avatarUrl(professionalData.getAvatarUrl())
                .specialties(professionalData.getSpecialties())
                .active(true)
                .build();
        
        return professionalRepository.save(newProfile);
    }

    private void validate(Professional professional) {
        if (professional.getName() == null || professional.getName().trim().isEmpty()) {
            throw new BusinessRuleException("O nome do profissional é obrigatório");
        }
    }
}
