package com.bookinghub.review.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(
            @Value("${app.openapi.server-url:http://localhost:8080/api/reviews}") String serverUrl) {
        return new OpenAPI()
                .info(new Info().title("Review Service API").version("v1")
                        .description("Post-service reviews and ratings for professionals and establishments"))
                .addServersItem(new Server().url(serverUrl).description("API Gateway"));
    }
}
