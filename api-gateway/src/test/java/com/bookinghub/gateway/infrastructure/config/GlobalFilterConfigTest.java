package com.bookinghub.gateway.infrastructure.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class GlobalFilterConfigTest {

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private ServerWebExchange.Builder exchangeBuilder;

    @Mock
    private ServerHttpRequest.Builder requestBuilder;

    private GlobalFilterConfig globalFilterConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        globalFilterConfig = new GlobalFilterConfig();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchangeBuilder.request(any(ServerHttpRequest.class))).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    void shouldAddCorrelationIdWhenMissing() {
        // Given
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        // When
        GlobalFilter filter = globalFilterConfig.correlationIdFilter();
        filter.filter(exchange, chain).block();

        // Then
        verify(requestBuilder).header(eq("X-Correlation-ID"), anyString());
        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldKeepExistingCorrelationId() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Correlation-ID", "existing-id");
        when(request.getHeaders()).thenReturn(headers);

        // When
        GlobalFilter filter = globalFilterConfig.correlationIdFilter();
        filter.filter(exchange, chain).block();

        // Then
        verify(requestBuilder).header("X-Correlation-ID", "existing-id");
        verify(chain).filter(any(ServerWebExchange.class));
    }
}
