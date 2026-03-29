package com.bookinghub.catalog.core.ports;

import com.bookinghub.catalog.core.domain.Affiliation;

public interface CatalogEventPublisher {
    void publishAffiliationCreated(Affiliation affiliation);
}
