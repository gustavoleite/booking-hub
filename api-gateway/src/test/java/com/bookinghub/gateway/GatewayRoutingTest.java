package com.bookinghub.gateway;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "AUTH_SERVICE_URI=http://localhost:${wiremock.server.port}",
                "spring.profiles.active=local"
        })
@AutoConfigureWireMock(port = 0)
class GatewayRoutingTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    void shouldRouteToAuthServiceWithRewrite() {
        stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"UP\"}")));

        webClient.get().uri("/api/auth/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void shouldReturnUnauthorizedWhenNoTokenOnProtectedRoute() {
        webClient.get().uri("/api/bookings")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
