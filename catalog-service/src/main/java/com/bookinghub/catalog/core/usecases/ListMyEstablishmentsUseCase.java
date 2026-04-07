package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListMyEstablishmentsUseCase {
  private final EstablishmentRepository establishmentRepository;

  public List<Establishment> execute(String ownerId) {
    return establishmentRepository.findByOwnerId(ownerId);
  }
}
