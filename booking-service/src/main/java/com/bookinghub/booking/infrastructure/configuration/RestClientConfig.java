package com.bookinghub.booking.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient catalogRestClient(
            @Value("${catalog.service.uri}") String catalogBaseUrl) {
        return RestClient.builder()
                .baseUrl(catalogBaseUrl)
                .build();
    }
}
