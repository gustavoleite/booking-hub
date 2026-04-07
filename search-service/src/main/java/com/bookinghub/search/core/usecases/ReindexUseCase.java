package com.bookinghub.search.core.usecases;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.ports.CatalogClient;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReindexUseCase {

    private final CatalogClient catalogClient;
    private final EstablishmentSearchRepository repository;

    public int execute() {
        if (log.isInfoEnabled()) {
            log.info("Starting reindex from catalog-service");
        }
        List<EstablishmentDocument> establishments = catalogClient.fetchAllEstablishments();
        if (log.isInfoEnabled()) {
            log.info("Fetched {} establishments from catalog-service", establishments.size());
        }
        for (EstablishmentDocument doc : establishments) {
            repository.upsert(doc);
        }
        if (log.isInfoEnabled()) {
            log.info("Reindex complete: {} documents indexed", establishments.size());
        }
        return establishments.size();
    }
}
