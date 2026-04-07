package com.bookinghub.search.core.ports;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import java.util.List;

public interface CatalogClient {
    List<EstablishmentDocument> fetchAllEstablishments();
}
