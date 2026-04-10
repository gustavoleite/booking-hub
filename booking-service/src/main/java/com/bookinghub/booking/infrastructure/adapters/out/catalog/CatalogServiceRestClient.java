package com.bookinghub.booking.infrastructure.adapters.out.catalog;

import com.bookinghub.booking.core.domain.DaySchedule;
import com.bookinghub.booking.core.domain.ScheduleInfo;
import com.bookinghub.booking.core.exceptions.CatalogServiceException;
import com.bookinghub.booking.core.ports.CatalogServiceClient;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class CatalogServiceRestClient implements CatalogServiceClient {

    private final RestClient catalogRestClient;

    public CatalogServiceRestClient(@Qualifier("catalogRestClient") RestClient catalogRestClient) {
        this.catalogRestClient = catalogRestClient;
    }

    @Override
    public ScheduleInfo getSchedule(UUID establishmentId, UUID professionalId, UUID serviceId) {
        try {
            ScheduleResponse response = catalogRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/establishments/{eid}/affiliations/professional/{pid}/schedule")
                            .queryParam("serviceId", serviceId)
                            .build(establishmentId, professionalId))
                    .retrieve()
                    .body(ScheduleResponse.class);

            if (response == null) {
                throw new CatalogServiceException("Empty response from catalog service");
            }

            List<DaySchedule> schedule = response.fixedSchedule() == null ? List.of() :
                    response.fixedSchedule().stream()
                            .map(d -> new DaySchedule(
                                    d.dayOfWeek(),
                                    LocalTime.parse(d.startTime()),
                                    LocalTime.parse(d.endTime())
                            ))
                            .toList();

            return new ScheduleInfo(
                    response.active(), response.price(), response.durationMinutes(), schedule);

        } catch (HttpClientErrorException.NotFound e) {
            throw new CatalogServiceException("Professional or service not found in catalog", e);
        } catch (CatalogServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch schedule from catalog service", e);
            throw new CatalogServiceException("Catalog service unavailable: " + e.getMessage(), e);
        }
    }
}
