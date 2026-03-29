package com.bookinghub.catalog.infrastructure.adapters.out.database;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaProfessionalRepository extends JpaRepository<ProfessionalEntity, UUID> {
    Optional<ProfessionalEntity> findByIdAndActiveTrue(UUID id);
}
