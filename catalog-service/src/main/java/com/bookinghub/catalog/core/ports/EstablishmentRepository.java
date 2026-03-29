package com.bookinghub.catalog.core.ports;

import com.bookinghub.catalog.core.domain.Establishment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstablishmentRepository {
    Establishment save(Establishment establishment);
    Optional<Establishment> findById(UUID id);
    List<Establishment> findByOwnerId(String ownerId);
    boolean existsByCnpj(String cnpj);
}
