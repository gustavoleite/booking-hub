package com.bookinghub.catalog.core.ports;

import com.bookinghub.catalog.core.domain.Affiliation;
import java.util.Optional;
import java.util.UUID;

public interface AffiliationRepository {
  Affiliation save(Affiliation affiliation);

  Optional<Affiliation> findById(UUID id);

  Optional<Affiliation> findByEstablishmentIdAndProfessionalId(
      UUID establishmentId, UUID professionalId);
}
