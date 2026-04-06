package com.bookinghub.search.core.usecases;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.ports.CatalogClient;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ReindexUseCase {

    private final CatalogClient catalogClient;
    private final EstablishmentSearchRepository repository;

    public int execute() {
        log.info("Starting reindex from catalog-service");
        List<EstablishmentDocument> establishments = catalogClient.fetchAllEstablishments();
        log.info("Fetched {} establishments from catalog-service", establishments.size());
        for (EstablishmentDocument doc : establishments) {
            repository.upsert(doc);
        }
        log.info("Reindex complete: {} documents indexed", establishments.size());
        return establishments.size();
    }
}
