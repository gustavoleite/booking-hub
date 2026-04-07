package com.bookinghub.catalog.core.ports;

import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.domain.Professional;

public interface CatalogEventPublisher {
  void publishEstablishmentCreated(Establishment establishment);

  void publishEstablishmentUpdated(Establishment establishment);

  void publishAffiliationCreated(
      Affiliation affiliation, Professional professional, Establishment establishment);

  void publishAffiliationUpdated(
      Affiliation affiliation, Professional professional, Establishment establishment);
}
