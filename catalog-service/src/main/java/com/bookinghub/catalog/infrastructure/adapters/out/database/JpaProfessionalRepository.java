package com.bookinghub.catalog.infrastructure.adapters.out.database;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProfessionalRepository extends JpaRepository<ProfessionalEntity, UUID> {
    Optional<ProfessionalEntity> findByIdAndActiveTrue(UUID id);
}
