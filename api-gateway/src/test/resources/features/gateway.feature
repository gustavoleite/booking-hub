Feature: API Gateway Functionalities
  As a developer
  I want the API Gateway to manage routing, security, and traceability
  So that I can access downstream services safely

  Scenario: Route to Auth Service (Public Route)
    Given the Auth Service is up and responding to "/health"
    When I request the Gateway at "/api/auth/health"
    Then I should receive a response with status 200
    And the response body should contain "{\"status\":\"UP\"}"

  Scenario: Route to Catalog Service with Token (Protected Route)
    Given the "Catalog" Service is up and responding to "/establishments" with "POST"
    And I have a valid JWT token
    When I request the Gateway at "/api/catalog/establishments" with "POST" and the token
    Then I should receive a response with status 201

  Scenario: Deny Access to Protected Catalog Route without Token
    Given the "Catalog" Service is up and responding to "/establishments" with "POST"
    And I do not provide a JWT token
    When I request the Gateway at "/api/catalog/establishments" with "POST"
    Then I should receive a response with status 401

  Scenario: Access Public Catalog Endpoint (No Token Required)
    Given the "Catalog" Service is up and responding to "/establishments/00000000-0000-0000-0000-000000000001" with "GET"
    When I request the Gateway at "/api/catalog/establishments/00000000-0000-0000-0000-000000000001" with "GET"
    Then I should receive a response with status 200

  Scenario: Propagate Correlation ID
    Given I request the Gateway at "/api/auth/health" with "GET"
    Then the response should contain a header "X-Correlation-ID"

  Scenario: Deny Access to Catalog with Invalid Token
    Given the "Catalog" Service is up and responding to "/establishments" with "POST"
    And I have an invalid JWT token
    When I request the Gateway at "/api/catalog/establishments" with "POST" and the token
    Then I should receive a response with status 401

  Scenario: Allow Access to Public Documentation
    When I request the Gateway at "/webjars/swagger-ui/index.html"
    Then I should receive a response with status 200
