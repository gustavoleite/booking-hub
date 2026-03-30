package com.bookinghub.gateway;

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
            if (path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.contains("/api-docs")) {
                return chain.filter(exchange);
            }

            // DONT filter if it's already rewritten (doesn't start with /api/)
            if (!path.startsWith("/api/")) {
                return chain.filter(exchange);
            }

            // Public endpoints that dont need JWT
            if (path.matches("^/api/catalog/establishments/[^/]+$") && request.getMethod().name().equals("GET")) {
                return chain.filter(exchange);
            }
            if (path.matches("^/api/catalog/professionals/[^/]+$") && request.getMethod().name().equals("GET")) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = jwtValidationService.validateTokenAndGetClaims(token);

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> r
                                .header("X-User-Id", claims.getSubject())
                                .header("X-User-Role", claims.get("role", String.class))
                        )
                        .build();

                return chain.filter(modifiedExchange);

            } catch (Exception e) {
                return onError(exchange, "Invalid JWT Token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
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
