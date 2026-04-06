package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.exceptions.ForbiddenException;
import com.bookinghub.catalog.core.exceptions.NotFoundException;
import com.bookinghub.catalog.core.ports.CatalogEventPublisher;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UpdateEstablishmentUseCase {
    private final EstablishmentRepository establishmentRepository;
    private final CatalogEventPublisher eventPublisher;

    public Establishment execute(UUID id, String ownerId, Establishment updatedData) {
        Establishment existing = establishmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Establishment not found"));

        if (!existing.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("Only the owner can update the establishment");
        }

        existing.updateDetails(updatedData.getName(), updatedData.getDescription(), updatedData.getPhotos(), updatedData.getAddress());

        Establishment saved = establishmentRepository.save(existing);
        eventPublisher.publishEstablishmentUpdated(saved);
        return saved;
    }
}
