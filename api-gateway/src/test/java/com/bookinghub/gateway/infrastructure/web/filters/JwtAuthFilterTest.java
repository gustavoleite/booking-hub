package com.bookinghub.gateway.infrastructure.web.filters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bookinghub.gateway.core.application.services.JwtValidationService;
import com.bookinghub.gateway.core.domain.exceptions.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class JwtAuthFilterTest {

    @Mock
    private JwtValidationService jwtValidationService;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthFilter = new JwtAuthFilter(jwtValidationService);

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(response.setStatusCode(any())).thenReturn(true);
        when(response.setComplete()).thenReturn(Mono.empty());
    }

    @Test
    void shouldSkipAuthenticationForSwaggerEndpoints() {
        // Given
        when(request.getURI()).thenReturn(URI.create("/v3/api-docs"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void shouldSkipAuthenticationForNonApiEndpoints() {
        // Given
        when(request.getURI()).thenReturn(URI.create("/actuator/health"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void shouldSkipAuthenticationForPublicGetEndpoints() {
        // Given — path must contain a valid UUID to match the filter's regex
        when(request.getURI()).thenReturn(URI.create("/api/catalog/establishments/00000000-0000-0000-0000-000000000001"));
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void shouldSkipAuthenticationForPublicSearchEndpoint() {
        // Given
        when(request.getURI()).thenReturn(URI.create("/api/search"));
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void shouldSkipAuthenticationForPublicAuthEndpoints() {
        // Given
        when(request.getURI()).thenReturn(URI.create("/api/auth/login"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void shouldSkipAuthenticationForPublicBookingsAvailabilityEndpoint() {
        // Given
        when(request.getURI()).thenReturn(URI.create("/api/bookings/availability"));
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void shouldSkipAuthenticationForPublicReviewEndpoints() {
        // Given
        String uuid = "00000000-0000-0000-0000-000000000001";
        when(request.getURI()).thenReturn(URI.create("/api/reviews/professional/" + uuid + "/stats"));
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void shouldSkipAuthenticationForPublicProfessionalReviewEndpoints() {
        // Given
        String uuid = "00000000-0000-0000-0000-000000000001";
        when(request.getURI()).thenReturn(URI.create("/api/reviews/professional/" + uuid));
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void shouldSkipAuthenticationForPublicEstablishmentReviewEndpoints() {
        // Given
        String uuid = "00000000-0000-0000-0000-000000000001";
        when(request.getURI()).thenReturn(URI.create("/api/reviews/establishment/" + uuid));
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void shouldSkipAuthenticationForPublicEstablishmentReviewStatsEndpoints() {
        // Given
        String uuid = "00000000-0000-0000-0000-000000000001";
        when(request.getURI()).thenReturn(URI.create("/api/reviews/establishment/" + uuid + "/stats"));
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void shouldSkipAuthenticationForPublicProfessionalEndpoints() {
        // Given
        String uuid = "00000000-0000-0000-0000-000000000001";
        when(request.getURI()).thenReturn(URI.create("/api/catalog/professionals/" + uuid));
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void shouldSkipAuthenticationForPublicProfessionalScheduleEndpoints() {
        // Given
        String uuid1 = "00000000-0000-0000-0000-000000000001";
        String uuid2 = "00000000-0000-0000-0000-000000000002";
        String path = "/api/catalog/establishments/" + uuid1 + "/affiliations/professional/" + uuid2 + "/schedule";
        when(request.getURI()).thenReturn(URI.create(path));
        when(request.getMethod()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtValidationService);
    }

    @Test
    void shouldSkipAuthenticationForApiDocs() {
        // Given
        when(request.getURI()).thenReturn(URI.create("/api-docs"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
    }

    @Test
    void shouldSkipAuthenticationForWebjars() {
        // Given
        when(request.getURI()).thenReturn(URI.create("/webjars/some-resource"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(chain).filter(exchange);
    }

    @Test
    void shouldReturnUnauthorizedWhenHeaderIsMissing() {
        // Given
        when(request.getURI()).thenReturn(URI.create("/api/orders"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnUnauthorizedWhenHeaderIsInvalid() {
        // Given
        when(request.getURI()).thenReturn(URI.create("/api/orders"));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "InvalidHeader");
        when(request.getHeaders()).thenReturn(headers);

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAddHeadersAndContinueWhenTokenIsValid() {
        // Given
        when(request.getURI()).thenReturn(URI.create("/api/orders"));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer valid-token");
        when(request.getHeaders()).thenReturn(headers);

        Claims claims = new DefaultClaims(Map.of("sub", "user123", "role", "ROLE_ADMIN"));
        when(jwtValidationService.validateTokenAndGetClaims("valid-token")).thenReturn(claims);

        ServerWebExchange.Builder exchangeBuilder = mock(ServerWebExchange.Builder.class);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(any(java.util.function.Consumer.class))).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(jwtValidationService).validateTokenAndGetClaims("valid-token");
        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() {
        // Given
        when(request.getURI()).thenReturn(URI.create("/api/orders"));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        when(request.getHeaders()).thenReturn(headers);

        when(jwtValidationService.validateTokenAndGetClaims("invalid-token")).thenThrow(new InvalidTokenException("Expired"));

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnInternalServerErrorWhenConfigIsInvalid() {
        // Given
        when(request.getURI()).thenReturn(URI.create("/api/orders"));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer valid-token");
        when(request.getHeaders()).thenReturn(headers);

        when(jwtValidationService.validateTokenAndGetClaims("valid-token"))
                .thenThrow(new com.bookinghub.gateway.core.domain.exceptions.JwtConfigurationException("Missing key"));

        // When
        GatewayFilter filter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
        filter.filter(exchange, chain).block();

        // Then
        verify(response).setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
