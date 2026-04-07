package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.domain.ProvidedService;
import com.bookinghub.catalog.core.exceptions.ForbiddenException;
import com.bookinghub.catalog.core.exceptions.NotFoundException;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddProvidedServiceUseCase {
  private final EstablishmentRepository establishmentRepository;

  public ProvidedService execute(UUID establishmentId, String ownerId, ProvidedService service) {
    Establishment establishment = establishmentRepository.findById(establishmentId)
        .orElseThrow(() -> new NotFoundException("Establishment not found"));

    if (!establishment.getOwnerId().equals(ownerId)) {
      throw new ForbiddenException("Only the owner can add services to the establishment");
    }

    ProvidedService toAdd = ProvidedService.builder()
        .id(UUID.randomUUID())
        .title(service.getTitle())
        .description(service.getDescription())
        .active(true)
        .build();

    establishment.addProvidedService(toAdd);
    establishmentRepository.save(establishment);

    return toAdd;
  }
}
