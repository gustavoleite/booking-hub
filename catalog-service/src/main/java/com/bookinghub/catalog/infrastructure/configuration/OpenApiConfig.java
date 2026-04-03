package com.bookinghub.catalog.infrastructure.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Insira o token JWT gerado pelo Auth Service."
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(
            @Value("${app.openapi.server-url:http://localhost:8080/api/catalog}") String serverUrl) {
        return new OpenAPI()
                .info(new Info().title("Catalog Service API").version("v1"))
                .addServersItem(new Server().url(serverUrl).description("API Gateway"));
    }
}
