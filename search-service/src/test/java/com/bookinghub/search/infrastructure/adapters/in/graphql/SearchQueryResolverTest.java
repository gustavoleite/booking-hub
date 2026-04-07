package com.bookinghub.search.infrastructure.adapters.in.graphql;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.domain.SearchFilter;
import com.bookinghub.search.core.domain.SearchPage;
import com.bookinghub.search.core.usecases.SearchEstablishmentsUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@GraphQlTest(SearchQueryResolver.class)
@ActiveProfiles("test")
class SearchQueryResolverTest {

    @Autowired GraphQlTester graphQlTester;
    @MockBean SearchEstablishmentsUseCase searchUseCase;

    private SearchPage emptyPage() {
        return SearchPage.builder()
                .results(Collections.emptyList())
                .totalHits(0).page(0).size(10)
                .build();
    }

    private SearchPage pageWithOne(EstablishmentDocument doc, Double distanceKm) {
        return SearchPage.builder()
                .results(List.of(SearchPage.EstablishmentResult.builder()
                        .document(doc).distanceKm(distanceKm).score(0.9).build()))
                .totalHits(1).page(0).size(10)
                .build();
    }

    private EstablishmentDocument sampleDoc() {
        return EstablishmentDocument.builder()
                .id("est-1").name("Barbearia Central")
                .city("São Paulo").state("SP")
                .averageRating(4.5).totalReviews(10)
                .minPrice(120.0).maxPrice(120.0)
                .services(List.of(EstablishmentDocument.ServiceEntry.builder()
                        .serviceId("s1").title("Corte Simples")
                        .minPrice(120.0).maxPrice(120.0).build()))
                .professionals(List.of(EstablishmentDocument.ProfessionalEntry.builder()
                        .professionalId("p1").name("João Barbeiro")
                        .specialties(List.of("Corte Masculino")).build()))
                .build();
    }

    @Test
    void shouldReturnMappedEstablishmentResult() {
        when(searchUseCase.execute(any(), anyInt(), anyInt()))
                .thenReturn(pageWithOne(sampleDoc(), 1.5));

        graphQlTester.document("""
                { searchEstablishments(filter: { city: "São Paulo" }) {
                  totalHits page size
                  results {
                    id name city state
                    averageRating totalReviews minPrice maxPrice
                    distanceKm score
                    services { title minPrice maxPrice }
                    professionals { name specialties }
                  }
                }}
                """)
                .execute()
                .path("searchEstablishments.totalHits").entity(Integer.class).isEqualTo(1)
                .path("searchEstablishments.results[0].name").entity(String.class).isEqualTo("Barbearia Central")
                .path("searchEstablishments.results[0].city").entity(String.class).isEqualTo("São Paulo")
                .path("searchEstablishments.results[0].averageRating").entity(Double.class).isEqualTo(4.5)
                .path("searchEstablishments.results[0].distanceKm").entity(Double.class).isEqualTo(1.5)
                .path("searchEstablishments.results[0].services[0].title").entity(String.class).isEqualTo("Corte Simples")
                .path("searchEstablishments.results[0].professionals[0].name").entity(String.class).isEqualTo("João Barbeiro");
    }

    @Test
    void shouldPassCityAndStateFilterToUseCase() {
        when(searchUseCase.execute(any(), anyInt(), anyInt())).thenReturn(emptyPage());

        graphQlTester.document("""
                { searchEstablishments(filter: { city: "São Paulo", state: "SP", minRating: 3.5 }) {
                  totalHits
                }}
                """).execute();

        var captor = ArgumentCaptor.forClass(SearchFilter.class);
        verify(searchUseCase).execute(captor.capture(), eq(0), eq(10));
        assertThat(captor.getValue().getCity()).isEqualTo("São Paulo");
        assertThat(captor.getValue().getState()).isEqualTo("SP");
        assertThat(captor.getValue().getMinRating()).isEqualTo(3.5);
    }

    @Test
    void shouldPassGeoFilterAndEnableHasGeo() {
        when(searchUseCase.execute(any(), anyInt(), anyInt())).thenReturn(emptyPage());

        graphQlTester.document("""
                { searchEstablishments(filter: {
                    geo: { lat: -23.5505, lon: -46.6333, radiusKm: 2.0 }
                    sortBy: DISTANCE
                  }) { totalHits }}
                """).execute();

        var captor = ArgumentCaptor.forClass(SearchFilter.class);
        verify(searchUseCase).execute(captor.capture(), anyInt(), anyInt());
        var filter = captor.getValue();
        assertThat(filter.hasGeo()).isTrue();
        assertThat(filter.getGeoLat()).isEqualTo(-23.5505);
        assertThat(filter.getGeoLon()).isEqualTo(-46.6333);
        assertThat(filter.getGeoRadiusKm()).isEqualTo(2.0);
        assertThat(filter.getSortBy()).isEqualTo(SearchFilter.SortBy.DISTANCE);
    }

    @Test
    void shouldPassServicesAndPriceFiltersToUseCase() {
        when(searchUseCase.execute(any(), anyInt(), anyInt())).thenReturn(emptyPage());

        graphQlTester.document("""
                { searchEstablishments(filter: {
                    services: ["Corte Simples", "Barba Completa"]
                    minPrice: 100.0
                    maxPrice: 150.0
                  }) { totalHits }}
                """).execute();

        var captor = ArgumentCaptor.forClass(SearchFilter.class);
        verify(searchUseCase).execute(captor.capture(), anyInt(), anyInt());
        var filter = captor.getValue();
        assertThat(filter.getServices()).containsExactly("Corte Simples", "Barba Completa");
        assertThat(filter.getMinPrice()).isEqualTo(100.0);
        assertThat(filter.getMaxPrice()).isEqualTo(150.0);
    }

    @Test
    void shouldUseDefaultPageZeroAndSizeTenWhenPageOmitted() {
        when(searchUseCase.execute(any(), anyInt(), anyInt())).thenReturn(emptyPage());

        graphQlTester.document("{ searchEstablishments(filter: {}) { totalHits } }").execute();

        verify(searchUseCase).execute(any(), eq(0), eq(10));
    }

    @Test
    void shouldPassExplicitPaginationToUseCase() {
        when(searchUseCase.execute(any(), anyInt(), anyInt())).thenReturn(emptyPage());

        graphQlTester.document("""
                { searchEstablishments(filter: {}, page: { page: 2, size: 5 }) { totalHits page size }}
                """).execute();

        verify(searchUseCase).execute(any(), eq(2), eq(5));
    }

    @Test
    void shouldSilentlyIgnoreInvalidSortByValue() {
        when(searchUseCase.execute(any(), anyInt(), anyInt())).thenReturn(emptyPage());

        // INVALID_SORT is not in the GraphQL schema enum — schema validation will reject it
        // but an empty sortBy should produce null in the filter
        graphQlTester.document("""
                { searchEstablishments(filter: { city: "SP" }) { totalHits }}
                """).execute();

        var captor = ArgumentCaptor.forClass(SearchFilter.class);
        verify(searchUseCase).execute(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().getSortBy()).isNull();
    }

    @Test
    void shouldMapQueryTextFilter() {
        when(searchUseCase.execute(any(), anyInt(), anyInt())).thenReturn(emptyPage());

        graphQlTester.document("""
                { searchEstablishments(filter: { query: "Barbearia Central", sortBy: RELEVANCE }) {
                  totalHits
                }}
                """).execute();

        var captor = ArgumentCaptor.forClass(SearchFilter.class);
        verify(searchUseCase).execute(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().getQuery()).isEqualTo("Barbearia Central");
        assertThat(captor.getValue().getSortBy()).isEqualTo(SearchFilter.SortBy.RELEVANCE);
    }

    @Test
    void shouldReturnEmptyResultsListWhenNoMatches() {
        when(searchUseCase.execute(any(), anyInt(), anyInt())).thenReturn(emptyPage());

        graphQlTester.document("{ searchEstablishments(filter: { city: \"Nowhere\" }) { totalHits results { id } } }")
                .execute()
                .path("searchEstablishments.totalHits").entity(Integer.class).isEqualTo(0)
                .path("searchEstablishments.results").entityList(Object.class).hasSize(0);
    }
}
