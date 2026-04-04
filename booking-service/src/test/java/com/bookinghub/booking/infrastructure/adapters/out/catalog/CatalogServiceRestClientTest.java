package com.bookinghub.booking.infrastructure.adapters.out.catalog;

import com.bookinghub.booking.core.domain.ScheduleInfo;
import com.bookinghub.booking.core.exceptions.CatalogServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceRestClientTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    private CatalogServiceRestClient client;

    @BeforeEach
    void setUp() {
        client = new CatalogServiceRestClient(restClient);
    }

    @Test
    void shouldGetSchedule() {
        UUID eid = UUID.randomUUID();
        UUID pid = UUID.randomUUID();
        UUID sid = UUID.randomUUID();

        ScheduleResponse response = new ScheduleResponse(
                true,
                new BigDecimal("100.00"),
                60,
                List.of(new ScheduleResponse.DayScheduleResponse(1, "08:00", "18:00"))
        );

        when(restClient.get()
                .uri(any(Function.class))
                .retrieve()
                .body(ScheduleResponse.class))
                .thenReturn(response);

        ScheduleInfo result = client.getSchedule(eid, pid, sid);

        assertThat(result.active()).isTrue();
        assertThat(result.price()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.durationMinutes()).isEqualTo(60);
        assertThat(result.workSchedule()).hasSize(1);
    }

    @Test
    void shouldGetScheduleWithNullFixedSchedule() {
        ScheduleResponse response = new ScheduleResponse(true, new BigDecimal("100.00"), 60, null);
        when(restClient.get().uri(any(Function.class)).retrieve().body(ScheduleResponse.class)).thenReturn(response);

        ScheduleInfo result = client.getSchedule(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        assertThat(result.workSchedule()).isEmpty();
    }

    @Test
    void shouldThrowWhenResponseIsNull() {
        when(restClient.get()
                .uri(any(Function.class))
                .retrieve()
                .body(ScheduleResponse.class))
                .thenReturn(null);

        assertThatThrownBy(() -> client.getSchedule(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(CatalogServiceException.class)
                .hasMessageContaining("Empty response");
    }

    @Test
    void shouldThrowWhenNotFound() {
        when(restClient.get()
                .uri(any(Function.class))
                .retrieve()
                .body(ScheduleResponse.class))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> client.getSchedule(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(CatalogServiceException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldThrowGenericException() {
        when(restClient.get()
                .uri(any(Function.class))
                .retrieve()
                .body(ScheduleResponse.class))
                .thenThrow(new RuntimeException("API down"));

        assertThatThrownBy(() -> client.getSchedule(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(CatalogServiceException.class)
                .hasMessageContaining("unavailable");
    }
}
