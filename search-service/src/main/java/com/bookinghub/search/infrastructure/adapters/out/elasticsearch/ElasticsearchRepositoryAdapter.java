package com.bookinghub.search.infrastructure.adapters.out.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.domain.SearchFilter;
import com.bookinghub.search.core.domain.SearchPage;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchRepositoryAdapter implements EstablishmentSearchRepository {

  private static final String INDEX = "establishments";
  private final ElasticsearchSearchRepository esRepository;
  private final ElasticsearchOperations operations;
  private final ElasticsearchClient elasticsearchClient;

  @Override
  public void upsert(EstablishmentDocument doc) {
    esRepository.save(toEsDocument(doc));
  }

  @Override
  public void upsertPartial(String id, Map<String, Object> fields) {
    Document update = Document.create();
    update.putAll(fields);
    operations.update(
        UpdateQuery.builder(id)
            .withDocument(update)
            .withDocAsUpsert(true)
            .build(),
        IndexCoordinates.of(INDEX)
    );
  }

  @Override
  public Optional<EstablishmentDocument> findById(String id) {
    return esRepository.findById(id).map(this::toDomain);
  }

  @Override
  public SearchPage search(SearchFilter filter, int page, int size) {
    try {
      var boolQuery = new BoolQuery.Builder();

      if (filter.getQuery() != null && !filter.getQuery().isBlank()) {
        boolQuery.must(m -> m.multiMatch(mm -> mm
                .query(filter.getQuery())
                .fields("name^3", "description", "services.title^2", "professionals.name^2")
        ));
      }
      if (filter.getCity() != null) {
        boolQuery.filter(f -> f.term(t -> t.field("city").value(filter.getCity())));
      }
      if (filter.getState() != null) {
        boolQuery.filter(f -> f.term(t -> t.field("state").value(filter.getState())));
      }
      if (filter.getMinRating() != null) {
        boolQuery.filter(
            f -> f.range(r -> r.field("averageRating").gte(JsonData.of(filter.getMinRating()))));
      }
      if (filter.getMinPrice() != null) {
        boolQuery.filter(
            f -> f.range(r -> r.field("minPrice").gte(JsonData.of(filter.getMinPrice()))));
      }
      if (filter.getMaxPrice() != null) {
        boolQuery.filter(
            f -> f.range(r -> r.field("maxPrice").lte(JsonData.of(filter.getMaxPrice()))));
      }
      if (filter.getServices() != null && !filter.getServices().isEmpty()) {
        for (String service : filter.getServices()) {
          boolQuery.filter(f -> f.nested(n -> n
                  .path("services")
                  .query(q -> q.match(m -> m.field("services.title").query(service)))
          ));
        }
      }
      if (filter.hasGeo()) {
        boolQuery.filter(f -> f.geoDistance(g -> g
                .field("geoPoint")
                .location(loc -> loc.latlon(
                    ll -> ll.lat(filter.getGeoLat()).lon(filter.getGeoLon())))
                .distance(filter.getGeoRadiusKm() + "km")
        ));
      }

      Query baseQuery = Query.of(q -> q.bool(boolQuery.build()));

      SearchFilter.SortBy sortBy = filter.getSortBy() != null
          ? filter.getSortBy() : SearchFilter.SortBy.RELEVANCE;

      if (sortBy == SearchFilter.SortBy.RELEVANCE) {
        Query finalBase = baseQuery;
        baseQuery = Query.of(q -> q.functionScore(fs -> fs
                .query(finalBase)
                .functions(fn -> fn.fieldValueFactor(fvf -> fvf
                        .field("averageRating")
                        .factor(0.5)
                        .modifier(FieldValueFactorModifier.Log1p)
                        .missing(1.0)
                ))
                .boostMode(FunctionBoostMode.Multiply)
        ));
      }

      Query finalQuery = baseQuery;
      SearchFilter.SortBy finalSortBy = sortBy;

      SearchResponse<EstablishmentEsDocument> response = elasticsearchClient.search(req -> {
        req.index(INDEX).from(page * size).size(size).query(finalQuery);

        if (finalSortBy == SearchFilter.SortBy.RATING) {
          req.sort(s -> s.field(f -> f.field("averageRating").order(SortOrder.Desc)));
        } else if (finalSortBy == SearchFilter.SortBy.DISTANCE && filter.hasGeo()) {
          req.sort(s -> s.geoDistance(g -> g
                  .field("geoPoint")
                  .location(loc -> loc.latlon(
                      ll -> ll.lat(filter.getGeoLat()).lon(filter.getGeoLon())))
                  .order(SortOrder.Asc)
                  .unit(co.elastic.clients.elasticsearch._types.DistanceUnit.Kilometers)
          ));
        }
        return req;
      }, EstablishmentEsDocument.class);

      List<SearchPage.EstablishmentResult> results = new ArrayList<>();
      for (Hit<EstablishmentEsDocument> hit : response.hits().hits()) {
        EstablishmentDocument doc =
            hit.source() != null ? toDomain(hit.source()) : new EstablishmentDocument();

        Double distanceKm = null;
        if (filter.hasGeo() && finalSortBy == SearchFilter.SortBy.DISTANCE
            && !hit.sort().isEmpty()) {
          try {
            distanceKm = Double.parseDouble(hit.sort().get(0).toString());
          } catch (Exception e) {
            log.debug("Could not parse sort distance", e);
          }
        }

        results.add(SearchPage.EstablishmentResult.builder()
            .document(doc)
            .distanceKm(distanceKm)
            .score(hit.score())
            .build());
      }

      long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;

      return SearchPage.builder()
          .results(results)
          .totalHits(totalHits)
          .page(page)
          .size(size)
          .build();

    } catch (IOException e) {
      log.error("Search query failed", e);
      return SearchPage.builder()
          .results(Collections.emptyList())
          .totalHits(0)
          .page(page)
          .size(size)
          .build();
    }
  }

  private EstablishmentEsDocument toEsDocument(EstablishmentDocument doc) {
    var esDoc = EstablishmentEsDocument.builder()
        .id(doc.getId())
        .name(doc.getName())
        .description(doc.getDescription())
        .city(doc.getCity())
        .state(doc.getState())
        .zipCode(doc.getZipCode())
        .minPrice(doc.getMinPrice())
        .maxPrice(doc.getMaxPrice())
        .ratingSum(doc.getRatingSum())
        .averageRating(doc.getAverageRating())
        .totalReviews(doc.getTotalReviews() != null ? doc.getTotalReviews() : 0)
        .build();

    if (doc.getLat() != null && doc.getLon() != null) {
      esDoc.setGeoPoint(new GeoPoint(doc.getLat(), doc.getLon()));
    }
    if (doc.getServices() != null) {
      esDoc.setServices(doc.getServices().stream().map(s ->
          EstablishmentEsDocument.ServiceEsEntry.builder()
              .serviceId(s.getServiceId())
              .title(s.getTitle())
              .minPrice(s.getMinPrice())
              .maxPrice(s.getMaxPrice())
              .build()
      ).toList());
    }
    if (doc.getProfessionals() != null) {
      esDoc.setProfessionals(doc.getProfessionals().stream().map(p ->
          EstablishmentEsDocument.ProfessionalEsEntry.builder()
              .professionalId(p.getProfessionalId())
              .name(p.getName())
              .specialties(p.getSpecialties())
              .build()
      ).toList());
    }
    return esDoc;
  }

  private EstablishmentDocument toDomain(EstablishmentEsDocument esDoc) {
    var builder = EstablishmentDocument.builder()
        .id(esDoc.getId())
        .name(esDoc.getName())
        .description(esDoc.getDescription())
        .city(esDoc.getCity())
        .state(esDoc.getState())
        .zipCode(esDoc.getZipCode())
        .minPrice(esDoc.getMinPrice())
        .maxPrice(esDoc.getMaxPrice())
        .ratingSum(esDoc.getRatingSum())
        .averageRating(esDoc.getAverageRating())
        .totalReviews(esDoc.getTotalReviews());

    if (esDoc.getGeoPoint() != null) {
      builder.lat(esDoc.getGeoPoint().getLat());
      builder.lon(esDoc.getGeoPoint().getLon());
    }
    if (esDoc.getServices() != null) {
      builder.services(esDoc.getServices().stream().map(s ->
          EstablishmentDocument.ServiceEntry.builder()
              .serviceId(s.getServiceId())
              .title(s.getTitle())
              .minPrice(s.getMinPrice())
              .maxPrice(s.getMaxPrice())
              .build()
      ).toList());
    }
    if (esDoc.getProfessionals() != null) {
      builder.professionals(esDoc.getProfessionals().stream().map(p ->
          EstablishmentDocument.ProfessionalEntry.builder()
              .professionalId(p.getProfessionalId())
              .name(p.getName())
              .specialties(p.getSpecialties())
              .build()
      ).toList());
    }
    return builder.build();
  }
}
