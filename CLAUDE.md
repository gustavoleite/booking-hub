# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Booking Hub** is an event-driven microservices platform (FIAP Tech Challenge) built with Spring Boot 3.2 / Java 21 / Maven. Currently has three operational services: `api-gateway`, `auth-service`, `catalog-service`. Planned but not yet implemented: `booking-service`, `review-service`, `search-service`, `notification-service`.

## Build & Run Commands

**Build all modules from root:**
```bash
mvn clean package -DskipTests
mvn clean verify          # with tests and JaCoCo coverage
```

**Run a single service locally (requires local PostgreSQL + RabbitMQ):**
```bash
cd auth-service && mvn spring-boot:run
cd catalog-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

**Run all services via Docker Compose (recommended):**
```bash
docker compose up -d            # start infrastructure + all services
docker compose up -d --build    # rebuild images first
docker compose down
docker compose logs -f [service-name]
```

**Testing:**
```bash
mvn test                        # all tests in a module
mvn test -pl auth-service       # tests for a specific module from root
mvn test -Dtest=MyTestClass     # single test class
mvn verify                      # tests + JaCoCo coverage report (target/site/jacoco/)
```
Cucumber reports are written to `target/cucumber-reports.html`.

## Architecture

**Communication patterns:**
- Client → API Gateway: REST/JSON, JWT Bearer token required (except `/api/auth/**`)
- Gateway → Services: REST/JSON, forwards `X-User-Id`, `X-User-Role`, `X-Correlation-ID` headers extracted from JWT
- Catalog → Booking (future): gRPC on port 9091 (`catalog-service/src/main/proto/catalog_service.proto`)
- Services → Events: RabbitMQ AMQP (catalog publishes `CatalogUpdated`; booking will publish `BookingCreated/Cancelled`)

**Security:** RS256 asymmetric JWT. Auth Service signs tokens with private key (`infra/certs/private_key.pem`). API Gateway validates with public key (`infra/certs/public_key.pem`). Downstream services trust the forwarded headers — they do NOT re-validate JWT.

**Roles:** `ROLE_CLIENT`, `ROLE_PROFESSIONAL`, `ROLE_OWNER` — enforced at gateway and in controller `@PreAuthorize` annotations.

## Code Structure (all three services follow this layout)

Each service uses **Clean / Hexagonal Architecture**:

```
core/
  domain/        # Pure business entities (no framework deps)
  usecases/      # Business logic, one class per use case
  ports/         # Interface contracts: Repository, Encoder, TokenService, etc.
  exceptions/    # Domain exceptions

application/
  dto/           # Request/Response DTOs

infrastructure/
  adapters/in/rest/       # Spring MVC controllers + global exception handler
  adapters/in/grpc/       # gRPC service impls (catalog-service only)
  adapters/out/database/  # JPA persistence adapters implementing core ports
  adapters/out/messaging/ # RabbitMQ publishers
  adapters/out/jwt/       # Token generation (Nimbus JOSE+JWT in auth-service)
  adapters/out/security/  # Password encoding (BCrypt)
  configuration/          # Spring @Configuration and @Bean definitions
```

Naming conventions:
- Use cases: `[Action][Entity]UseCase` (e.g., `RegisterUserUseCase`)
- Repository ports: `[Entity]Repository`; adapters: `Postgres[Entity]RepositoryAdapter`
- DTOs live in `application/dto/`, never in `core/`

## Configuration Profiles

Each service has three profiles: **local** (default, uses `localhost`), **docker** (uses Docker DNS service names), **test** (H2 in-memory, loaded by `application-test.yml`).

Profile is set via `spring.profiles.active` env var. Docker Compose sets `SPRING_PROFILES_ACTIVE=docker`.

Key environment variables in docker mode: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`, `RABBIT_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `RSA_PUBLIC_KEY_PATH` (gateway), `RSA_PRIVATE_KEY_PATH` (auth), `AUTH_SERVICE_URI`, `CATALOG_SERVICE_URI`, `CORS_ALLOWED_ORIGINS`.

## Testing Approach

Three layers of tests:

1. **Unit tests** — `core/domain` and `core/usecases`, plain JUnit 5 + Mockito, no Spring context.
2. **Component/integration tests** — Spring Boot Test with H2 (`application-test.yml`) or Testcontainers (PostgreSQL) for adapter-level tests.
3. **BDD / acceptance tests** — Cucumber + REST Assured. Feature files in `src/test/resources/features/`. Run as part of `mvn test`. Scenarios cover routing, JWT validation, CORS, registration, login, establishment and professional management.

JaCoCo minimum coverage: **80% LINE**. Excluded from coverage: JPA entities, DTOs, gRPC generated classes.

## Database

PostgreSQL with **Flyway** auto-migration on startup. Migration scripts: `src/main/resources/db/migration/`.

- `auth_db`: `tb_users`, `tb_user_roles`
- `catalog_db`: `tb_establishments`, `tb_business_hours`, `tb_provided_services`, `tb_professionals`, `tb_affiliations`, `tb_work_schedules`, `tb_service_offerings`

Local init SQL (creates both databases): `infra/init-scripts/init.sql`.

## Access Points (local)

| Service | REST | Other |
|---------|------|-------|
| API Gateway | http://localhost:8080 (Swagger: `/swagger-ui.html`) | — |
| Auth Service | http://localhost:8081 (Swagger: `/swagger-ui.html`) | — |
| Catalog Service | http://localhost:8083 (Swagger: `/swagger-ui.html`) | gRPC: 9091 |
| RabbitMQ UI | http://localhost:15672 (guest/guest) | AMQP: 5672 |
| PostgreSQL | localhost:5432 | — |
