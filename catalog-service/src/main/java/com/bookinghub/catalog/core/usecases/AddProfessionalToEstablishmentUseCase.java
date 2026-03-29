package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.*;
import com.bookinghub.catalog.core.ports.AffiliationRepository;
import com.bookinghub.catalog.core.ports.CatalogEventPublisher;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class AddProfessionalToEstablishmentUseCase {
    private final EstablishmentRepository establishmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final AffiliationRepository affiliationRepository;
    private final CatalogEventPublisher eventPublisher;

    public Affiliation execute(UUID establishmentId, UUID professionalId, Affiliation affiliation) {
        Establishment establishment = establishmentRepository.findById(establishmentId)
                .orElseThrow(() -> new RuntimeException("Establishment not found"));

        professionalRepository.findById(professionalId)
                .orElseThrow(() -> new RuntimeException("Professional not found"));

        // Business Rule 1: Professional schedule must be within establishment business hours
        for (WorkSchedule schedule : affiliation.getWorkSchedules()) {
            BusinessHour salonHour = establishment.getDefaultBusinessHours().stream()
                    .filter(bh -> bh.getDayOfWeek() == schedule.getDayOfWeek())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Salon is closed on day " + schedule.getDayOfWeek()));

            if (!salonHour.isWithin(schedule.getStartTime(), schedule.getEndTime())) {
                throw new RuntimeException("Horário do profissional fora do expediente do salão");
            }
        }

        // Domain invariant: Overlapping check is inside affiliation.updateSchedules, but we can do it here too if we are setting them
        affiliation.updateSchedules(affiliation.getWorkSchedules());

        Affiliation savedAffiliation = affiliationRepository.save(affiliation);
        eventPublisher.publishAffiliationCreated(savedAffiliation);
        return savedAffiliation;
    }
}
