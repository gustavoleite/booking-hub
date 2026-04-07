package com.bookinghub.search.infrastructure.adapters.in.graphql;

import com.bookinghub.search.application.dto.EstablishmentFilterInput;
import com.bookinghub.search.application.dto.EstablishmentResultResponse;
import com.bookinghub.search.application.dto.PageInput;
import com.bookinghub.search.application.dto.SearchResultResponse;
import com.bookinghub.search.core.domain.SearchFilter;
import com.bookinghub.search.core.domain.SearchPage;
import com.bookinghub.search.core.usecases.SearchEstablishmentsUseCase;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SearchQueryResolver {

  private final SearchEstablishmentsUseCase searchEstablishmentsUseCase;

  @QueryMapping
  public SearchResultResponse searchEstablishments(
            @Argument("filter") EstablishmentFilterInput filter,
            @Argument("page") PageInput page
  ) {
    SearchFilter searchFilter = mapFilter(filter);
    int pageNum = page != null && page.page() != null ? page.page() : 0;
    int pageSize = page != null && page.size() != null ? page.size() : 10;

    SearchPage result = searchEstablishmentsUseCase.execute(searchFilter, pageNum, pageSize);

    List<EstablishmentResultResponse> results = result.getResults().stream()
        .map(r -> {
          var doc = r.getDocument();
          List<EstablishmentResultResponse.ServiceSummaryResponse> services =
              doc.getServices() != null
              ? doc.getServices().stream()
              .map(s -> new EstablishmentResultResponse.ServiceSummaryResponse(
                  s.getTitle(), s.getMinPrice(), s.getMaxPrice()))
              .toList()
              : Collections.emptyList();

          List<EstablishmentResultResponse.ProfessionalSummaryResponse> professionals =
              doc.getProfessionals() != null
              ? doc.getProfessionals().stream()
              .map(p -> new EstablishmentResultResponse.ProfessionalSummaryResponse(
                  p.getName(),
                  p.getSpecialties() != null ? p.getSpecialties() : Collections.emptyList()))
              .toList()
              : Collections.emptyList();

          return new EstablishmentResultResponse(
              doc.getId(),
              doc.getName(),
              doc.getDescription(),
              doc.getCity() != null ? doc.getCity() : "",
              doc.getState() != null ? doc.getState() : "",
              services,
              professionals,
              doc.getMinPrice(),
              doc.getMaxPrice(),
              doc.getAverageRating(),
              doc.getTotalReviews() != null ? doc.getTotalReviews() : 0,
              r.getDistanceKm(),
              r.getScore()
          );
        })
        .toList();

    return new SearchResultResponse(
        results, result.getTotalHits(), result.getPage(), result.getSize());
  }

  private SearchFilter mapFilter(EstablishmentFilterInput filter) {
    if (filter == null) {
      return SearchFilter.builder().build();
    }

    var builder = SearchFilter.builder();

    if (filter.query() != null) {
      builder.query(filter.query());
    }
    if (filter.city() != null) {
      builder.city(filter.city());
    }
    if (filter.state() != null) {
      builder.state(filter.state());
    }
    if (filter.minRating() != null) {
      builder.minRating(filter.minRating());
    }
    if (filter.minPrice() != null) {
      builder.minPrice(filter.minPrice());
    }
    if (filter.maxPrice() != null) {
      builder.maxPrice(filter.maxPrice());
    }
    if (filter.services() != null) {
      builder.services(filter.services());
    }
    if (filter.sortBy() != null) {
      try {
        builder.sortBy(SearchFilter.SortBy.valueOf(filter.sortBy()));
      } catch (IllegalArgumentException ignored) {
        // Use default sort when invalid value provided
      }
    }
    if (filter.geo() != null) {
      var geo = filter.geo();
      if (geo.lat() != null && geo.lon() != null && geo.radiusKm() != null) {
        builder.geoLat(geo.lat()).geoLon(geo.lon()).geoRadiusKm(geo.radiusKm());
      }
    }

    return builder.build();
  }
}
