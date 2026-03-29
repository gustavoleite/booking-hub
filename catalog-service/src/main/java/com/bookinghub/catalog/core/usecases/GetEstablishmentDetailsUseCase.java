package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.exceptions.NotFoundException;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetEstablishmentDetailsUseCase {
    private final EstablishmentRepository establishmentRepository;

    public Establishment execute(UUID id) {
        return establishmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Establishment not found"));
    }
}
