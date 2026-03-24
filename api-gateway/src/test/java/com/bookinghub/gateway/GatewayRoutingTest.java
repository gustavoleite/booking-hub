package com.bookinghub.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"AUTH_SERVICE_URI=http://localhost:${wiremock.server.port}"})
@AutoConfigureWireMock(port = 0)
class GatewayRoutingTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    void shouldRouteToAuthService() {
        stubFor(get(urlEqualTo("/api/auth/health"))
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
