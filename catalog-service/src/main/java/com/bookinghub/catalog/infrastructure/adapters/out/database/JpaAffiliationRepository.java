package com.bookinghub.catalog.infrastructure.adapters.out.database;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAffiliationRepository extends JpaRepository<AffiliationEntity, UUID> {
  Optional<AffiliationEntity> findByEstablishmentIdAndProfessionalId(
      UUID establishmentId, UUID professionalId);
}
