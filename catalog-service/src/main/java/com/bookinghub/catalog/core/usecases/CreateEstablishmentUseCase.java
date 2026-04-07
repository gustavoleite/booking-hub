package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.BusinessHour;
import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.exceptions.ConflictException;
import com.bookinghub.catalog.core.ports.CatalogEventPublisher;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CreateEstablishmentUseCase {
    private final EstablishmentRepository establishmentRepository;
    private final CatalogEventPublisher eventPublisher;

    public Establishment execute(Establishment establishment) {
        String cleanCnpj = establishment.getCnpj() != null ? establishment.getCnpj().replaceAll("\\D", "") : null;
        Establishment establishmentWithCleanCnpj = Establishment.builder()
                .id(establishment.getId())
                .ownerId(establishment.getOwnerId())
                .name(establishment.getName())
                .cnpj(cleanCnpj)
                .description(establishment.getDescription())
                .active(establishment.isActive())
                .photos(establishment.getPhotos())
                .address(establishment.getAddress())
                .defaultBusinessHours(establishment.getDefaultBusinessHours())
                .providedServices(establishment.getProvidedServices())
                .build();

        validate(establishmentWithCleanCnpj);

        if (establishmentRepository.existsByCnpj(establishmentWithCleanCnpj.getCnpj())) {
            throw new ConflictException("Establishment with CNPJ " + establishmentWithCleanCnpj.getCnpj() + " already exists");
        }

        Establishment toSave = Establishment.builder()
                .id(UUID.randomUUID())
                .ownerId(establishmentWithCleanCnpj.getOwnerId())
                .name(establishmentWithCleanCnpj.getName())
                .cnpj(establishmentWithCleanCnpj.getCnpj())
                .description(establishmentWithCleanCnpj.getDescription())
                .active(true)
                .photos(establishmentWithCleanCnpj.getPhotos())
                .address(establishmentWithCleanCnpj.getAddress())
                .defaultBusinessHours(establishmentWithCleanCnpj.getDefaultBusinessHours())
                .providedServices(establishmentWithCleanCnpj.getProvidedServices())
                .build();

        Establishment saved = establishmentRepository.save(toSave);
        eventPublisher.publishEstablishmentCreated(saved);
        return saved;
    }

    private void validate(Establishment establishment) {
        if (establishment.getCnpj() == null || !isValidCnpj(establishment.getCnpj())) {
            throw new BusinessRuleException("Invalid CNPJ. Must have 14 digits.");
        }

        if (establishment.getAddress() == null) {
            throw new BusinessRuleException("O endereço é obrigatório");
        }

        if (establishment.getAddress().getLatitude() == null || establishment.getAddress().getLongitude() == null) {
            throw new BusinessRuleException("Endereço deve conter latitude e longitude");
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

    private boolean isValidCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14 || cnpj.matches("^(\\d)\\1{13}$")) {
            return false;
        }

        try {
            int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

            int sum1 = 0;
            for (int i = 0; i < 12; i++) {
                sum1 += Character.getNumericValue(cnpj.charAt(i)) * weights1[i];
            }
            int digit1 = 11 - (sum1 % 11);
            if (digit1 >= 10) { digit1 = 0; }

            int sum2 = 0;
            for (int i = 0; i < 13; i++) {
                sum2 += Character.getNumericValue(cnpj.charAt(i)) * weights2[i];
            }
            int digit2 = 11 - (sum2 % 11);
            if (digit2 >= 10) { digit2 = 0; }

            return Character.getNumericValue(cnpj.charAt(12)) == digit1 &&
                    Character.getNumericValue(cnpj.charAt(13)) == digit2;
        } catch (Exception e) {
            return false;
        }
    }
}
