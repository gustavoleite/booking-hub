package com.bookinghub.search.core.usecases;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class IndexEstablishmentUseCase {

  private final EstablishmentSearchRepository repository;

  public void execute(EstablishmentDocument doc) {
    if (log.isInfoEnabled()) {
      log.info("Indexing establishment {}", doc.getId());
    }
    repository.upsert(doc);
  }
}
