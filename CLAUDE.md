# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Booking Hub** is an event-driven microservices platform (FIAP Tech Challenge) built with Spring Boot 3.2 / Java 21 / Maven. Operational services: `api-gateway`, `auth-service`, `catalog-service`, `booking-service`, `review-service`, `search-service`. Planned but not yet implemented: `notification-service`.

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
- Client → API Gateway: REST/JSON, JWT Bearer token required (except `/api/auth/**` and public search/review endpoints)
- Gateway → Services: REST/JSON, forwards `X-User-Id`, `X-User-Role`, `X-Correlation-ID` headers extracted from JWT
- Services → Events: RabbitMQ AMQP
  - `catalog.events` exchange: `establishment.created`, `establishment.updated`, `affiliation.created`, `affiliation.updated`
  - `booking.events` exchange: `booking.completed`
  - `review.events` exchange: `review.created`
- search-service consumes all of the above to maintain an Elasticsearch read model (CQRS)

**Security:** RS256 asymmetric JWT. Auth Service signs tokens with private key (`infra/certs/private_key.pem`). API Gateway validates with public key (`infra/certs/public_key.pem`). Downstream services trust the forwarded headers — they do NOT re-validate JWT.

**Roles:** `ROLE_CLIENT`, `ROLE_PROFESSIONAL`, `ROLE_OWNER` — enforced at gateway and in controller `@PreAuthorize` annotations.

## Code Structure (all services follow this layout)

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

Each service has three profiles: **local** (default, uses `localhost`), **docker** (uses Docker DNS service names), **test** (embedded/testcontainers, loaded by `application-test.yml`).

Profile is set via `spring.profiles.active` env var. Docker Compose sets `SPRING_PROFILES_ACTIVE=docker`.

Key environment variables in docker mode: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`, `RABBIT_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `RSA_PUBLIC_KEY_PATH` (gateway), `RSA_PRIVATE_KEY_PATH` (auth), `AUTH_SERVICE_URI`, `CATALOG_SERVICE_URI`, `CORS_ALLOWED_ORIGINS`.

review-service uses MongoDB: `MONGO_HOST`, `MONGO_DB` (default: `review_db`).
search-service uses Elasticsearch: `ELASTICSEARCH_HOST`, `ELASTICSEARCH_PORT`, `CATALOG_SERVICE_URI`.

## Testing Approach

Three layers of tests:

1. **Unit tests** — `core/domain` and `core/usecases`, plain JUnit 5 + Mockito, no Spring context.
2. **Component/integration tests** — Spring Boot Test with embedded DB or Testcontainers for adapter-level tests.
   - `auth-service`, `catalog-service`, `booking-service`: H2 in-memory for BDD tests.
   - `review-service`: Flapdoodle embedded MongoDB (`de.flapdoodle.embed.mongo.spring30x:4.11.0`), version `7.0.2`.
   - `search-service`: Testcontainers `ElasticsearchContainer` (image `elasticsearch:8.13.0`, `xpack.security.enabled=false`).
3. **BDD / acceptance tests** — Cucumber + REST Assured. Feature files in `src/test/resources/features/`. Run as part of `mvn test`.

JaCoCo minimum coverage: **80% LINE**. Excluded from coverage: JPA/Mongo/ES entities, DTOs.

## Database

**PostgreSQL + Flyway** (auth-service, catalog-service, booking-service). Migration scripts: `src/main/resources/db/migration/`.

- `auth_db`: `tb_users`, `tb_user_roles`
- `catalog_db`: `tb_establishments` (includes `latitude`, `longitude`, `city`, `state`), `tb_business_hours`, `tb_provided_services`, `tb_professionals`, `tb_affiliations`, `tb_work_schedules`, `tb_service_offerings`
- `booking_db`: booking and slot tables

Local init SQL (creates all PG databases): `infra/init-scripts/init.sql`.

**MongoDB** (review-service). Database: `review_db`. Collections: `reviews`, `eligible_bookings`. No schema migrations — Spring Data MongoDB creates collections automatically.

**Elasticsearch 8.13** (search-service). Index: `establishments`. Geo-search enabled (`geo_point`). `xpack.security.enabled=false` in dev/docker.

## Access Points (local)

| Service | REST | Other |
|---------|------|-------|
| API Gateway | http://localhost:8080 (Swagger: `/swagger-ui.html`) | — |
| Auth Service | http://localhost:8081 (Swagger: `/swagger-ui.html`) | — |
| Booking Service | http://localhost:8082 (Swagger: `/swagger-ui.html`) | — |
| Catalog Service | http://localhost:8083 (Swagger: `/swagger-ui.html`) | — |
| Review Service | http://localhost:8084 (Swagger: `/swagger-ui.html`) | — |
| Search Service | http://localhost:8085 (GraphiQL: `/graphiql`) | GraphQL: `/graphql` |
| RabbitMQ UI | http://localhost:15672 (guest/guest) | AMQP: 5672 |
| PostgreSQL | localhost:5432 | — |
| MongoDB | localhost:27017 | — |
| Elasticsearch | http://localhost:9200 | — |
