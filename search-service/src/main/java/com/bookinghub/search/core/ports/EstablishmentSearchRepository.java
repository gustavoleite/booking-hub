package com.bookinghub.search.core.ports;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.domain.SearchFilter;
import com.bookinghub.search.core.domain.SearchPage;

import java.util.Map;
import java.util.Optional;

public interface EstablishmentSearchRepository {
    void upsert(EstablishmentDocument doc);
    void upsertPartial(String id, Map<String, Object> fields);
    Optional<EstablishmentDocument> findById(String id);
    SearchPage search(SearchFilter filter, int page, int size);
}
