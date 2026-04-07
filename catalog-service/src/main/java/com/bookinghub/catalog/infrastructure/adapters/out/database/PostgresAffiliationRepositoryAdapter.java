package com.bookinghub.catalog.infrastructure.adapters.out.database;

import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.domain.ServiceOffering;
import com.bookinghub.catalog.core.domain.WorkSchedule;
import com.bookinghub.catalog.core.ports.AffiliationRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresAffiliationRepositoryAdapter implements AffiliationRepository {
    private final JpaAffiliationRepository jpaRepository;

    @Override
    public Affiliation save(Affiliation affiliation) {
        AffiliationEntity entity = jpaRepository.save(toEntity(affiliation));
        return toDomain(entity);
    }

    @Override
    public Optional<Affiliation> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Affiliation> findByEstablishmentIdAndProfessionalId(
      UUID establishmentId, UUID professionalId) {
        return jpaRepository
                .findByEstablishmentIdAndProfessionalId(establishmentId, professionalId)
                .map(this::toDomain);
    }

    private AffiliationEntity toEntity(Affiliation domain) {
        AffiliationEntity entity = AffiliationEntity.builder()
                .id(domain.getId())
                .establishment(EstablishmentEntity.builder()
                        .id(domain.getEstablishmentId()).build())
                .professional(ProfessionalEntity.builder().id(domain.getProfessionalId()).build())
                .active(domain.isActive())
                .build();

        if (domain.getWorkSchedules() != null) {
            entity.setWorkSchedules(domain.getWorkSchedules().stream()
                    .map(ws -> WorkScheduleEntity.builder()
                            .affiliation(entity)
                            .dayOfWeek(ws.getDayOfWeek())
                            .startTime(ws.getStartTime())
                            .endTime(ws.getEndTime())
                            .build())
                    .collect(Collectors.toList()));
        }

        if (domain.getServiceOfferings() != null) {
            entity.setServiceOfferings(domain.getServiceOfferings().stream()
                    .map(so -> ServiceOfferingEntity.builder()
                            .affiliation(entity)
                            .providedService(ProvidedServiceEntity.builder()
                                    .id(so.getProvidedServiceId()).build())
                            .price(so.getPrice())
                            .durationMinutes(so.getDurationMinutes())
                            .build())
                    .collect(Collectors.toList()));
        }

        return entity;
    }

    private Affiliation toDomain(AffiliationEntity entity) {
        return Affiliation.builder()
                .id(entity.getId())
                .establishmentId(entity.getEstablishment().getId())
                .professionalId(entity.getProfessional().getId())
                .active(entity.isActive())
                .workSchedules(entity.getWorkSchedules().stream()
                        .map(ws -> WorkSchedule.builder()
                                .dayOfWeek(ws.getDayOfWeek())
                                .startTime(ws.getStartTime())
                                .endTime(ws.getEndTime())
                                .build())
                        .collect(Collectors.toList()))
                .serviceOfferings(entity.getServiceOfferings().stream()
                        .map(so -> ServiceOffering.builder()
                                .id(so.getId())
                                .providedServiceId(so.getProvidedService().getId())
                                .price(so.getPrice())
                                .durationMinutes(so.getDurationMinutes())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
