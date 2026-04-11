package com.bookinghub.notification.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${app.openapi.server-url:http://localhost:8080/api/calendar}")
    private String serverUrl;

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("ICS calendar feed for syncing BookingHub appointments with"
                                + " Google Calendar, Outlook and Apple Calendar")
                        .version("1.0.0"))
                .servers(List.of(new Server().url(serverUrl)));
    }
}
