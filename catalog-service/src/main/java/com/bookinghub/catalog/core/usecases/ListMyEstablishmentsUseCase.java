package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListMyEstablishmentsUseCase {
    private final EstablishmentRepository establishmentRepository;

    public List<Establishment> execute(String ownerId) {
        return establishmentRepository.findByOwnerId(ownerId);
    }
}
