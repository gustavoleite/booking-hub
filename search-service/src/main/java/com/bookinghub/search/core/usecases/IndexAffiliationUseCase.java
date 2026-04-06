package com.bookinghub.search.core.usecases;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class IndexAffiliationUseCase {

    private final EstablishmentSearchRepository repository;

    public void execute(
            String establishmentId,
            String professionalId,
            String professionalName,
            List<String> specialties,
            boolean active,
            List<EstablishmentDocument.ServiceEntry> serviceOfferings
    ) {
        log.info("Indexing affiliation for establishment {} professional {}", establishmentId, professionalId);

        Optional<EstablishmentDocument> existing = repository.findById(establishmentId);
        if (existing.isEmpty()) {
            log.warn("Establishment {} not yet indexed, skipping affiliation indexing", establishmentId);
            return;
        }

        EstablishmentDocument doc = existing.get();

        List<EstablishmentDocument.ProfessionalEntry> professionals = doc.getProfessionals() != null
                ? new ArrayList<>(doc.getProfessionals())
                : new ArrayList<>();
        professionals.removeIf(p -> p.getProfessionalId().equals(professionalId));
        if (active) {
            professionals.add(EstablishmentDocument.ProfessionalEntry.builder()
                    .professionalId(professionalId)
                    .name(professionalName)
                    .specialties(specialties)
                    .build());
        }

        List<EstablishmentDocument.ServiceEntry> services = doc.getServices() != null
                ? new ArrayList<>(doc.getServices())
                : new ArrayList<>();
        for (EstablishmentDocument.ServiceEntry offering : serviceOfferings) {
            services.removeIf(s -> s.getServiceId().equals(offering.getServiceId()));
            services.add(offering);
        }

        double newMin = services.stream()
                .filter(s -> s.getMinPrice() != null)
                .mapToDouble(EstablishmentDocument.ServiceEntry::getMinPrice)
                .min().orElse(0);
        double newMax = services.stream()
                .filter(s -> s.getMaxPrice() != null)
                .mapToDouble(EstablishmentDocument.ServiceEntry::getMaxPrice)
                .max().orElse(0);

        repository.upsertPartial(establishmentId, Map.of(
                "professionals", professionals,
                "services", services,
                "minPrice", newMin,
                "maxPrice", newMax
        ));
    }
}
