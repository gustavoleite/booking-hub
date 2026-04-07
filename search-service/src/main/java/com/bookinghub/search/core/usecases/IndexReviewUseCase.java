package com.bookinghub.search.core.usecases;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class IndexReviewUseCase {

  private final EstablishmentSearchRepository repository;

  public void execute(String establishmentId, Double establishmentRating) {
    if (establishmentRating == null || establishmentId == null) {
      log.debug("Skipping review indexing: missing establishmentId or rating");
      return;
    }
    log.info("Indexing review for establishment {}, rating={}",
        establishmentId, establishmentRating);

    Optional<EstablishmentDocument> existing = repository.findById(establishmentId);
    if (existing.isEmpty()) {
      log.warn("Establishment {} not indexed, skipping review update", establishmentId);
      return;
    }

    EstablishmentDocument doc = existing.get();
    double ratingSum =
        (doc.getRatingSum() != null ? doc.getRatingSum() : 0.0) + establishmentRating;
    int totalReviews = (doc.getTotalReviews() != null ? doc.getTotalReviews() : 0) + 1;
    double averageRating = Math.round(ratingSum / totalReviews * 10.0) / 10.0;

    Map<String, Object> fields = new HashMap<>();
    fields.put("ratingSum", ratingSum);
    fields.put("totalReviews", totalReviews);
    fields.put("averageRating", averageRating);
    repository.upsertPartial(establishmentId, fields);
  }
}
