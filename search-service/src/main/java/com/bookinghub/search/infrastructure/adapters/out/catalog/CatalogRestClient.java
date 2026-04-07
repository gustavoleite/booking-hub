package com.bookinghub.search.infrastructure.adapters.out.catalog;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.ports.CatalogClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class CatalogRestClient implements CatalogClient {

    private final RestClient restClient;

    public CatalogRestClient(@Value("${catalog.service.uri:http://localhost:8083}") String catalogUri) {
        this.restClient = RestClient.builder()
                .baseUrl(catalogUri)
                .build();
    }

    @Override
    public List<EstablishmentDocument> fetchAllEstablishments() {
        try {
            EstablishmentListResponse response = restClient.get()
                    .uri("/establishments")
                    .header("X-User-Id", "system-reindex")
                    .header("X-User-Role", "ROLE_OWNER")
                    .retrieve()
                    .body(EstablishmentListResponse.class);
            if (response == null || response.establishments() == null) {
                return Collections.emptyList();
            }
            return response.establishments().stream()
                    .map(this::toDocument)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to fetch establishments from catalog-service", e);
            return Collections.emptyList();
        }
    }

    private EstablishmentDocument toDocument(EstablishmentDto dto) {
        return EstablishmentDocument.builder()
                .id(dto.id())
                .name(dto.name())
                .description(dto.description())
                .city(dto.address() != null ? dto.address().city() : null)
                .state(dto.address() != null ? dto.address().state() : null)
                .zipCode(dto.address() != null ? dto.address().zipCode() : null)
                .lat(dto.address() != null && dto.address().latitude() != null
                        ? dto.address().latitude().doubleValue() : null)
                .lon(dto.address() != null && dto.address().longitude() != null
                        ? dto.address().longitude().doubleValue() : null)
                .services(Collections.emptyList())
                .professionals(Collections.emptyList())
                .totalReviews(0)
                .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record EstablishmentListResponse(List<EstablishmentDto> establishments) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record EstablishmentDto(String id, String name, String description, AddressDto address) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AddressDto(String city, String state, String zipCode,
      BigDecimal latitude, BigDecimal longitude) {
    }
}
