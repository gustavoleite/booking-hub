package com.bookinghub.catalog.infrastructure.adapters.out.database;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaAffiliationRepository extends JpaRepository<AffiliationEntity, UUID> {
    Optional<AffiliationEntity> findByEstablishmentIdAndProfessionalId(UUID establishmentId, UUID professionalId);
}
