package com.bookinghub.booking.infrastructure.adapters.out.auth;

import com.bookinghub.booking.core.ports.AuthServiceClient;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAuthServiceClient implements AuthServiceClient {

    private final RestClient authRestClient;

    @Override
    public String getUserEmail(UUID userId) {
        try {
            Map<String, String> response = authRestClient.get()
                    .uri("/internal/users/{id}/email", userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {});
            return response != null ? response.get("email") : null;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User not found in auth-service: {}", userId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch email for user {} from auth-service", userId, e);
            return null;
        }
    }
}
