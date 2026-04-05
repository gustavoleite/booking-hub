package com.bookinghub.gateway.infrastructure.web.filters;

import com.bookinghub.gateway.core.application.services.JwtValidationService;
import com.bookinghub.gateway.core.domain.exceptions.InvalidAuthorizationHeaderException;
import com.bookinghub.gateway.core.domain.exceptions.JwtConfigurationException;
import com.bookinghub.gateway.core.domain.exceptions.MissingTokenException;
import com.bookinghub.gateway.core.domain.exceptions.UnauthorizedException;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtValidationService jwtValidationService;

    public JwtAuthFilter(JwtValidationService jwtValidationService) {
        super(Config.class);
        this.jwtValidationService = jwtValidationService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Skip authentication for Swagger and API Docs
            if (path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.contains("/api-docs") || path.contains("/webjars")) {
                return chain.filter(exchange);
            }

            // DONT filter if it's already rewritten (doesn't start with /api/)
            if (!path.startsWith("/api/")) {
                return chain.filter(exchange);
            }

            // Auth service endpoints are public (registration and login)
            if (path.startsWith("/api/auth/")) {
                return chain.filter(exchange);
            }

            // Public endpoints that dont need JWT (UUID-identified resources only)
            String uuidPattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
            if (path.matches("^/api/catalog/establishments/" + uuidPattern + "$") && request.getMethod().name().equals("GET")) {
                return chain.filter(exchange);
            }
            if (path.matches("^/api/catalog/professionals/" + uuidPattern + "$") && request.getMethod().name().equals("GET")) {
                return chain.filter(exchange);
            }
            if (path.matches("^/api/catalog/establishments/" + uuidPattern + "/affiliations/professional/" + uuidPattern + "/schedule$") && request.getMethod().name().equals("GET")) {
                return chain.filter(exchange);
            }
            if (path.equals("/api/bookings/availability") && request.getMethod().name().equals("GET")) {
                return chain.filter(exchange);
            }

            // Public review endpoints (listing and stats — no auth required)
            if (path.matches("^/api/reviews/professional/" + uuidPattern + "(/stats)?$") && request.getMethod().name().equals("GET")) {
                return chain.filter(exchange);
            }
            if (path.matches("^/api/reviews/establishment/" + uuidPattern + "(/stats)?$") && request.getMethod().name().equals("GET")) {
                return chain.filter(exchange);
            }

            try {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new MissingTokenException("Missing Authorization Header");
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new InvalidAuthorizationHeaderException("Invalid Authorization Header");
                }

                String token = authHeader.substring(7);
                Claims claims = jwtValidationService.validateTokenAndGetClaims(token);

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> r
                                .header("X-User-Id", claims.getSubject())
                                .header("X-User-Role", claims.get("role", String.class))
                        )
                        .build();

                return chain.filter(modifiedExchange);

            } catch (UnauthorizedException e) {
                return onError(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (JwtConfigurationException e) {
                return onError(exchange, "Gateway configuration error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                return onError(exchange, "Authentication failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        System.err.println("Auth Filter Error: " + err);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
