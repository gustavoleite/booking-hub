package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.exceptions.ProfessionalNotFoundException;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UpdateProfessionalProfileUseCase {
    private final ProfessionalRepository professionalRepository;

    public Professional execute(UUID id, Professional professionalData) {
        validate(professionalData);
        
        return professionalRepository.findById(id)
                .map(existing -> {
                    existing.updateProfile(
                            professionalData.getName(),
                            professionalData.getBio(),
                            professionalData.getAvatarUrl(),
                            professionalData.getSpecialties()
                    );
                    return professionalRepository.save(existing);
                })
                .orElseThrow(() -> new ProfessionalNotFoundException("Perfil profissional não encontrado para o ID: " + id));
    }

    private void validate(Professional professional) {
        if (professional.getName() == null || professional.getName().trim().isEmpty()) {
            throw new BusinessRuleException("O nome do profissional é obrigatório");
        }
    }
}
