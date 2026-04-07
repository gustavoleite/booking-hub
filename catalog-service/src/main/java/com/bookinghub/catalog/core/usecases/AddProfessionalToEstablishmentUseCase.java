package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.domain.BusinessHour;
import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.domain.WorkSchedule;
import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.exceptions.NotFoundException;
import com.bookinghub.catalog.core.ports.AffiliationRepository;
import com.bookinghub.catalog.core.ports.CatalogEventPublisher;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddProfessionalToEstablishmentUseCase {
    private final EstablishmentRepository establishmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final AffiliationRepository affiliationRepository;
    private final CatalogEventPublisher eventPublisher;

    public Affiliation execute(UUID establishmentId, UUID professionalId, Affiliation affiliation) {
        Establishment establishment = establishmentRepository.findById(establishmentId)
                .orElseThrow(
                        () -> new NotFoundException(
                                "Estabelecimento não encontrado: " + establishmentId));

        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new NotFoundException(
                        "Profissional não encontrado: " + professionalId));

        List<WorkSchedule> schedules = affiliation.getWorkSchedules() != null
                ? affiliation.getWorkSchedules()
                : Collections.emptyList();

        List<BusinessHour> businessHours = establishment.getDefaultBusinessHours() != null
                ? establishment.getDefaultBusinessHours()
                : Collections.emptyList();

        for (WorkSchedule schedule : schedules) {
            BusinessHour salonHour = businessHours.stream()
                    .filter(bh -> bh.getDayOfWeek() == schedule.getDayOfWeek())
                    .findFirst()
                    .orElseThrow(
                            () -> new BusinessRuleException(
                                    "O salão não funciona no dia " + schedule.getDayOfWeek()));

            if (!salonHour.isWithin(schedule.getStartTime(), schedule.getEndTime())) {
                throw new BusinessRuleException(
                        "Horário do profissional fora do expediente do salão");
            }
        }

        affiliation.updateSchedules(schedules);

        // Reuse existing affiliation ID if one already exists for this pair
        Optional<Affiliation> existing = affiliationRepository
                .findByEstablishmentIdAndProfessionalId(establishmentId, professionalId);
        boolean isNew = existing.isEmpty();
        Affiliation toSave = existing.map(ex -> Affiliation.builder()
                .id(ex.getId())
                .establishmentId(affiliation.getEstablishmentId())
                .professionalId(affiliation.getProfessionalId())
                .active(affiliation.isActive())
                .workSchedules(affiliation.getWorkSchedules())
                .serviceOfferings(affiliation.getServiceOfferings())
                .build()).orElse(affiliation);

        Affiliation savedAffiliation = affiliationRepository.save(toSave);
        if (isNew) {
            eventPublisher.publishAffiliationCreated(savedAffiliation, professional, establishment);
        } else {
            eventPublisher.publishAffiliationUpdated(savedAffiliation, professional, establishment);
        }
        return savedAffiliation;
    }
}
