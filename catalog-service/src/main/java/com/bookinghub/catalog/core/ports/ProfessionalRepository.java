package com.bookinghub.catalog.core.ports;

import com.bookinghub.catalog.core.domain.Professional;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalRepository {
  Professional save(Professional professional);

  Optional<Professional> findById(UUID id);
}
