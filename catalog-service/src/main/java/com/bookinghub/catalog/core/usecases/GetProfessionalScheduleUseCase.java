package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.exceptions.NotFoundException;
import com.bookinghub.catalog.core.ports.AffiliationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetProfessionalScheduleUseCase {
    private final AffiliationRepository affiliationRepository;

    public Affiliation execute(UUID establishmentId, UUID professionalId) {
        return affiliationRepository
                .findByEstablishmentIdAndProfessionalId(establishmentId, professionalId)
                .orElseThrow(() -> new NotFoundException(
                        "Afiliação não encontrada para o profissional"
                                + " e estabelecimento informados"));
    }
}
