package com.bookinghub.search.infrastructure.adapters.in.graphql;

import com.bookinghub.search.application.dto.EstablishmentResultResponse;
import com.bookinghub.search.application.dto.SearchResultResponse;
import com.bookinghub.search.core.domain.SearchFilter;
import com.bookinghub.search.core.domain.SearchPage;
import com.bookinghub.search.core.usecases.SearchEstablishmentsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SearchQueryResolver {

    private final SearchEstablishmentsUseCase searchEstablishmentsUseCase;

    @QueryMapping
    public SearchResultResponse searchEstablishments(
            @Argument Map<String, Object> filter,
            @Argument Map<String, Object> page
    ) {
        SearchFilter searchFilter = mapFilter(filter);
        int pageNum = page != null && page.get("page") != null ? (int) page.get("page") : 0;
        int pageSize = page != null && page.get("size") != null ? (int) page.get("size") : 10;

        SearchPage result = searchEstablishmentsUseCase.execute(searchFilter, pageNum, pageSize);

        List<EstablishmentResultResponse> results = result.getResults().stream()
                .map(r -> {
                    var doc = r.getDocument();
                    List<EstablishmentResultResponse.ServiceSummaryResponse> services = doc.getServices() != null
                            ? doc.getServices().stream()
                                    .map(s -> new EstablishmentResultResponse.ServiceSummaryResponse(
                                            s.getTitle(), s.getMinPrice(), s.getMaxPrice()))
                                    .toList()
                            : Collections.emptyList();

                    List<EstablishmentResultResponse.ProfessionalSummaryResponse> professionals = doc.getProfessionals() != null
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

        return new SearchResultResponse(results, result.getTotalHits(), result.getPage(), result.getSize());
    }

    @SuppressWarnings("unchecked")
    private SearchFilter mapFilter(Map<String, Object> filter) {
        if (filter == null) return SearchFilter.builder().build();

        var builder = SearchFilter.builder();

        if (filter.get("query") instanceof String q) builder.query(q);
        if (filter.get("city") instanceof String c) builder.city(c);
        if (filter.get("state") instanceof String s) builder.state(s);
        if (filter.get("minRating") instanceof Number n) builder.minRating(n.doubleValue());
        if (filter.get("minPrice") instanceof Number n) builder.minPrice(n.doubleValue());
        if (filter.get("maxPrice") instanceof Number n) builder.maxPrice(n.doubleValue());
        if (filter.get("services") instanceof List<?> list) {
            builder.services(list.stream().map(Object::toString).toList());
        }
        if (filter.get("sortBy") instanceof String sortBy) {
            try {
                builder.sortBy(SearchFilter.SortBy.valueOf(sortBy));
            } catch (IllegalArgumentException ignored) {}
        }

        if (filter.get("geo") instanceof Map<?, ?> rawGeo) {
            Map<String, Object> geo = (Map<String, Object>) rawGeo;
            if (geo.get("lat") instanceof Number lat &&
                geo.get("lon") instanceof Number lon &&
                geo.get("radiusKm") instanceof Number radius) {
                builder.geoLat(lat.doubleValue())
                       .geoLon(lon.doubleValue())
                       .geoRadiusKm(radius.doubleValue());
            }
        }

        return builder.build();
    }
}
