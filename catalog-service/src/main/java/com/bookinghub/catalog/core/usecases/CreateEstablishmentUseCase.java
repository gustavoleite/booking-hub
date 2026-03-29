package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.BusinessHour;
import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.exceptions.ConflictException;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CreateEstablishmentUseCase {
    private final EstablishmentRepository establishmentRepository;

    public Establishment execute(Establishment establishment) {
        validate(establishment);

        if (establishmentRepository.existsByCnpj(establishment.getCnpj())) {
            throw new ConflictException("Establishment with CNPJ " + establishment.getCnpj() + " already exists");
        }

        Establishment toSave = Establishment.builder()
                .id(UUID.randomUUID())
                .ownerId(establishment.getOwnerId())
                .name(establishment.getName())
                .cnpj(establishment.getCnpj())
                .description(establishment.getDescription())
                .active(true)
                .photos(establishment.getPhotos())
                .address(establishment.getAddress())
                .defaultBusinessHours(establishment.getDefaultBusinessHours())
                .providedServices(establishment.getProvidedServices())
                .build();

        return establishmentRepository.save(toSave);
    }

    private void validate(Establishment establishment) {
        if (establishment.getCnpj() == null || !establishment.getCnpj().matches("\\d{14}")) {
            throw new BusinessRuleException("Invalid CNPJ. Must have 14 digits.");
        }

        if (establishment.getAddress() == null) {
            throw new BusinessRuleException("O endereço é obrigatório");
        }

        if (establishment.getProvidedServices() == null || establishment.getProvidedServices().isEmpty()) {
            throw new BusinessRuleException("Establishment must have at least one service.");
        }

        if (establishment.getDefaultBusinessHours() != null) {
            for (BusinessHour bh : establishment.getDefaultBusinessHours()) {
                if (!bh.getCloseTime().isAfter(bh.getOpenTime())) {
                    throw new BusinessRuleException("Close time must be after open time for day " + bh.getDayOfWeek());
                }
            }
        }
    }
}
