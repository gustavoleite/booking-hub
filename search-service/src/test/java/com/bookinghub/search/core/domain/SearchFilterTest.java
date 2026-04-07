package com.bookinghub.search.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SearchFilterTest {

    @Test
    void hasGeo_shouldReturnTrueWhenAllThreeFieldsPresent() {
        var filter = SearchFilter.builder()
                .geoLat(-23.5).geoLon(-46.6).geoRadiusKm(5.0)
                .build();
        assertThat(filter.hasGeo()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            ",      -46.6, 5.0",   // null lat
            "-23.5,      , 5.0",   // null lon
            "-23.5, -46.6,    ",   // null radius
    })
    void hasGeo_shouldReturnFalseWhenAnyFieldIsMissing(Double lat, Double lon, Double radius) {
        var filter = SearchFilter.builder()
                .geoLat(lat).geoLon(lon).geoRadiusKm(radius)
                .build();
        assertThat(filter.hasGeo()).isFalse();
    }

    @Test
    void hasGeo_shouldReturnFalseWhenAllGeoFieldsNull() {
        var filter = SearchFilter.builder().city("São Paulo").build();
        assertThat(filter.hasGeo()).isFalse();
    }
}
