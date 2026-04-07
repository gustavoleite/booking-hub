package com.bookinghub.gateway.bdd;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("local")
public class GatewaySteps {

    @Autowired
    private WebTestClient webClient;

    private WebTestClient.ResponseSpec response;
    private String validToken;
    private static final KeyPair keyPair;

    static {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        String publicKeyPem = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        registry.add("RSA_PUBLIC_KEY", () -> publicKeyPem);
        registry.add("AUTH_SERVICE_URI", () -> "http://localhost:${wiremock.server.port}");
        registry.add("BOOKING_SERVICE_URI", () -> "http://localhost:${wiremock.server.port}");
        registry.add("CATALOG_SERVICE_URI", () -> "http://localhost:${wiremock.server.port}");
        registry.add("REVIEW_SERVICE_URI", () -> "http://localhost:${wiremock.server.port}");
        registry.add("SEARCH_SERVICE_URI", () -> "http://localhost:${wiremock.server.port}");
    }

    @Given("the {string} Service is up and responding to {string} with {string}")
    public void serviceIsUpWithMethod(String service, String path, String method) {
        int status = method.equalsIgnoreCase("POST") ? 201 : 200;
        MappingBuilder mapping;
        if (method.equalsIgnoreCase("POST")) {
            mapping = post(urlEqualTo(path));
        } else {
            mapping = get(urlEqualTo(path));
        }

        stubFor(mapping
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(method.equalsIgnoreCase("POST") ? "{\"id\":\"123\"}" : "{\"status\":\"UP\"}")));
    }

    @Given("the Auth Service is up and responding to {string}")
    public void authServiceIsUp(String path) {
        stubFor(get(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"UP\"}")));
    }

    @Given("I have a valid JWT token")
    public void haveValidToken() {
        validToken = Jwts.builder()
                .setSubject("test-user")
                .claim("role", "ROLE_USER")
                .setExpiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    @Given("I do not provide a JWT token")
    public void noToken() {
        validToken = null;
    }

    @Given("I have an invalid JWT token")
    public void haveInvalidToken() {
        validToken = "invalid.token.string";
    }

    @When("I request the Gateway at {string}")
    public void requestGateway(String path) {
        response = webClient.get().uri(path).exchange();
    }

    @When("I request the Gateway at {string} with {string}")
    public void requestGatewayWithMethod(String path, String method) {
        WebTestClient.RequestHeadersSpec<?> spec;
        if (method.equalsIgnoreCase("POST")) {
            spec = webClient.post().uri(path);
        } else {
            spec = webClient.get().uri(path);
        }
        response = spec.exchange();
    }

    @When("I request the Gateway at {string} with {string} and the token")
    public void requestWithToken(String path, String method) {
        WebTestClient.RequestHeadersSpec<?> spec;
        if (method.equalsIgnoreCase("POST")) {
            spec = webClient.post().uri(path);
        } else {
            spec = webClient.get().uri(path);
        }
        response = spec
                .header("Authorization", "Bearer " + validToken)
                .exchange();
    }

    @When("I request the Gateway at {string} with the token")
    public void requestWithTokenSimple(String path) {
        response = webClient.get().uri(path)
                .header("Authorization", "Bearer " + validToken)
                .exchange();
    }

    @Then("I should receive a response with status {int}")
    public void verifyStatus(int status) {
        response.expectStatus().isEqualTo(status);
    }

    @And("the response body should contain {string}")
    public void verifyBody(String content) {
        response.expectBody().json(content);
    }

    @Then("the response should contain a header {string}")
    public void verifyHeader(String headerName) {
        response.expectHeader().exists(headerName);
    }
}
