package com.bookinghub.gateway.infrastructure.config;

import java.util.UUID;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;

@Configuration
public class GlobalFilterConfig {

  @Bean
  public GlobalFilter correlationIdFilter() {
    return (exchange, chain) -> {
      String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");

      if (correlationId == null || correlationId.isEmpty()) {
        correlationId = UUID.randomUUID().toString();
      }

      String finalCorrelationId = correlationId;

      ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
          .header("X-Correlation-ID", finalCorrelationId)
          .build();

      exchange.getResponse().getHeaders().add("X-Correlation-ID", finalCorrelationId);

      return chain.filter(exchange.mutate().request(mutatedRequest).build());
    };
  }
}
