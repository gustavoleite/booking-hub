package com.bookinghub.search.core.usecases;

import com.bookinghub.search.core.domain.SearchFilter;
import com.bookinghub.search.core.domain.SearchPage;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SearchEstablishmentsUseCase {

    private final EstablishmentSearchRepository repository;

    public SearchPage execute(SearchFilter filter, int page, int size) {
        return repository.search(filter, page, size);
    }
}
