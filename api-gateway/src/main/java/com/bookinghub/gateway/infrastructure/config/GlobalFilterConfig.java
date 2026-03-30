package com.bookinghub.gateway.infrastructure.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.UUID;

@Configuration
public class GlobalFilterConfig {

    @Bean
    public GlobalFilter correlationIdFilter() {
        return (exchange, chain) -> {
            String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");

            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-Correlation-ID", correlationId)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }
}
