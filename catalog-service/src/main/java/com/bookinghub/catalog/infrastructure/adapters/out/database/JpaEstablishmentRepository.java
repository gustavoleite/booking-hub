package com.bookinghub.catalog.infrastructure.adapters.out.database;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEstablishmentRepository extends JpaRepository<EstablishmentEntity, UUID> {
  Optional<EstablishmentEntity> findByIdAndActiveTrue(UUID id);

  List<EstablishmentEntity> findByOwnerIdAndActiveTrue(String ownerId);

  boolean existsByCnpj(String cnpj);
}
