# RFC: Review Service

**Status:** Proposta  
**Data:** 2026-04-04  
**Autor:** Booking Hub Team

---

## 1. Contexto e Motivação

O `booking-service` já gerencia o ciclo de vida completo de um agendamento e publica o evento `booking.completed` quando um serviço é concluído. O próximo passo lógico é o **`review-service`** — responsável por coletar e expor avaliações de clientes sobre profissionais e estabelecimentos.

O requisito 4 do Tech Challenge estabelece que clientes devem poder deixar avaliações e comentários após o serviço, com o objetivo de ajudar outros usuários a tomar decisões informadas. Isso implica três responsabilidades principais:

1. **Elegibilidade controlada:** apenas clientes com agendamentos concluídos podem avaliar, e cada agendamento pode gerar no máximo uma avaliação.
2. **Duplo alvo:** a avaliação cobre tanto o profissional quanto o estabelecimento em um único submit.
3. **Consulta pública:** as avaliações e médias devem ser consultáveis sem autenticação para embasar buscas e filtros.

---

## 2. Requisitos (origem: Tech-Challenge-Fase-3.md §4)

| # | Requisito |
|---|-----------|
| R1 | Após o serviço, clientes podem deixar avaliações e comentários sobre o estabelecimento e o profissional |
| R2 | As avaliações ajudam outros usuários a tomar decisões informadas (acesso público) |
| R3 | Integração com a busca/filtragem por avaliação (consumido pelo `search-service` futuro) |
| R4 | Alta cobertura de testes com TDD + BDD + JaCoCo ≥ 80% |

---

## 3. Domínio

### 3.1 Entidade Principal: `Review`

```
Review
├── id:                   UUID
├── bookingId:            UUID       ← chave única; vincula avaliação ao agendamento
├── clientId:             String     ← X-User-Id do JWT
├── professionalId:       UUID       ← referência ao catalog-service (snapshot)
├── establishmentId:      UUID       ← referência ao catalog-service (snapshot)
├── professionalRating:   Integer    ← 1–5, opcional (null se cliente não quis avaliar)
├── establishmentRating:  Integer    ← 1–5, opcional
├── comment:              String     ← texto livre, opcional
└── createdAt:            LocalDateTime
```

**Invariantes de domínio:**
- Pelo menos um dos ratings (`professionalRating` ou `establishmentRating`) deve estar presente.
- Rating válido: entre 1 e 5 inclusive.
- Uma review por `bookingId` — único constraint de banco.
- Apenas o `clientId` do booking concluído pode criar a review.

### 3.2 Entidade de Suporte: `EligibleBooking`

Para validar elegibilidade sem chamada síncrona ao `booking-service`, o review-service mantém localmente um registro dos bookings concluídos consumidos via RabbitMQ:

```
EligibleBooking
├── bookingId:         UUID   (PK)
├── clientId:          String
├── professionalId:    UUID
├── establishmentId:   UUID
└── completedAt:       LocalDateTime
```

Esse registro é criado ao consumir `booking.completed` e consultado na criação de uma review.

---

## 4. Casos de Uso

| Use Case | Actor | Descrição |
|----------|-------|-----------|
| `ConsumeBookingCompletedUseCase` | Sistema (RabbitMQ listener) | Persiste `EligibleBooking` ao receber `booking.completed` |
| `CreateReviewUseCase` | `ROLE_CLIENT` | Valida elegibilidade, garante unicidade, persiste review |
| `GetReviewsByProfessionalUseCase` | Público | Lista reviews de um profissional com média e total |
| `GetReviewsByEstablishmentUseCase` | Público | Lista reviews de um estabelecimento com média e total |
| `GetReviewByBookingUseCase` | `ROLE_CLIENT` (dono), `ROLE_PROFESSIONAL`, `ROLE_OWNER` | Retorna a review de um booking específico |

---

## 5. API REST

Prefixo no gateway: `/api/reviews/**` → porta interna `8084`

### 5.1 Endpoints

```
POST /reviews
     Body: { bookingId, professionalRating?, establishmentRating?, comment? }
     → ReviewResponse (201)
     → ROLE_CLIENT
     → Erros: 400 (nenhum rating), 403 (booking não pertence ao cliente),
              409 (booking já avaliado), 422 (booking não concluído / inelegível)

GET  /reviews/professional/{professionalId}
     ?page={n}&size={n}
     → Page<ReviewSummary>
     → Público (sem autenticação)

GET  /reviews/professional/{professionalId}/stats
     → { averageRating, totalReviews }
     → Público

GET  /reviews/establishment/{establishmentId}
     ?page={n}&size={n}
     → Page<ReviewSummary>
     → Público

GET  /reviews/establishment/{establishmentId}/stats
     → { averageRating, totalReviews }
     → Público

GET  /reviews/booking/{bookingId}
     → ReviewResponse ou 404
     → ROLE_CLIENT (dono do booking), ROLE_PROFESSIONAL ou ROLE_OWNER
```

### 5.2 Request/Response

**Request `POST /reviews`:**
```json
{
  "bookingId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "professionalRating": 5,
  "establishmentRating": 4,
  "comment": "Atendimento excelente, ambiente muito agradável."
}
```

**Response `201 Created`:**
```json
{
  "id": "a1b2c3d4-0000-0000-0000-000000000001",
  "bookingId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "clientId": "26cc6703-b9f2-476d-9d3d-ac6f1dec37b0",
  "professionalId": "b6840d12-c231-4dc4-b623-df5b23298f13",
  "establishmentId": "e7e666e0-1589-4ead-b7a5-c8e05795cc48",
  "professionalRating": 5,
  "establishmentRating": 4,
  "comment": "Atendimento excelente, ambiente muito agradável.",
  "createdAt": "2026-04-04T18:00:00"
}
```

**Response `GET /reviews/professional/{id}/stats`:**
```json
{
  "professionalId": "b6840d12-c231-4dc4-b623-df5b23298f13",
  "averageRating": 4.7,
  "totalReviews": 42
}
```

**Response `GET /reviews/professional/{id}` (paginado):**
```json
{
  "content": [
    {
      "id": "a1b2c3d4-...",
      "professionalRating": 5,
      "establishmentRating": 4,
      "comment": "Atendimento excelente.",
      "createdAt": "2026-04-04T18:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5
}
```

---

## 6. Fluxo de Elegibilidade via Eventos

Esta é a decisão de design mais importante do serviço: **como validar que um booking existe, está concluído e pertence ao cliente — sem chamada síncrona ao `booking-service`?**

### 6.1 Solução: Tabela Local de Bookings Elegíveis

O review-service **consome o evento `booking.completed`** do RabbitMQ e persiste localmente os dados mínimos necessários para validação:

```
booking-service                RabbitMQ               review-service
      │                           │                         │
      │── booking.completed ─────▶│── [listener] ──────────▶│
      │   {bookingId,             │                         │ INSERT tb_eligible_bookings
      │    clientId,              │                         │ (bookingId, clientId,
      │    professionalId,        │                         │  professionalId,
      │    establishmentId, ...}  │                         │  establishmentId,
      │                           │                         │  completedAt)
```

Quando `POST /reviews` é chamado:

```
1. Busca EligibleBooking por bookingId → 422 se não encontrado (não concluído)
2. Verifica clientId == X-User-Id do JWT → 403 se divergir
3. Verifica que não existe Review para esse bookingId → 409 se existir
4. Valida ratings (1–5, ao menos um presente) → 400 se inválido
5. Persiste Review
```

### 6.2 Por que não chamar o booking-service de forma síncrona?

| Abordagem | Prós | Contras |
|-----------|------|---------|
| **Evento + tabela local (escolhida)** | Sem acoplamento síncrono; resiliente a falhas do booking-service; baixa latência | Eventual consistency (janela entre completar e poder avaliar ≈ < 1s) |
| REST síncrono ao booking-service | Dados sempre frescos | Acoplamento temporal; falha no booking-service derruba o review; latência de rede extra |

A eventual consistency aqui é aceitável: o cliente não irá avaliar o serviço no mesmo milissegundo em que o profissional clicou em "concluir". Na prática, a janela é imperceptível.

---

## 7. Integração com RabbitMQ

### 7.1 Evento Consumido

Exchange: `booking.events` (topic, durável) — já declarado pelo `booking-service`

| Routing Key | Payload | Ação no review-service |
|-------------|---------|------------------------|
| `booking.completed` | `{bookingId, clientId, professionalId, establishmentId, providedServiceId, startDatetime, endDatetime, price, durationMinutes, status, occurredAt}` | Persiste `EligibleBooking` |

**Queue dedicada:** `review.booking.completed` com binding `booking.completed` no exchange `booking.events`.

```java
// infrastructure/configuration/RabbitMQConfig.java
@Bean Queue reviewBookingCompletedQueue() {
    return QueueBuilder.durable("review.booking.completed").build();
}

@Bean Binding reviewBookingCompletedBinding(Queue reviewBookingCompletedQueue, TopicExchange bookingEventsExchange) {
    return BindingBuilder
        .bind(reviewBookingCompletedQueue)
        .to(bookingEventsExchange)
        .with("booking.completed");
}
```

### 7.2 Evento Publicado (futuro)

| Routing Key | Payload | Consumidores futuros |
|-------------|---------|----------------------|
| `review.created` | `{reviewId, bookingId, professionalId, establishmentId, professionalRating, establishmentRating}` | `search-service` (índice de avaliação) |

> O evento `review.created` não é obrigatório para a fase 3, mas é declarado aqui para garantir que o exchange `review.events` seja criado desde o início, facilitando a integração futura com o `search-service`.

---

## 8. Banco de Dados

### 8.1 Schema (Flyway V1)

```sql
-- Bookings elegíveis para avaliação (consumidos do RabbitMQ)
CREATE TABLE tb_eligible_bookings (
    booking_id        UUID         PRIMARY KEY,
    client_id         VARCHAR(255) NOT NULL,
    professional_id   UUID         NOT NULL,
    establishment_id  UUID         NOT NULL,
    completed_at      TIMESTAMP    NOT NULL
);

-- Avaliações
CREATE TABLE tb_reviews (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id           UUID         NOT NULL UNIQUE,
    client_id            VARCHAR(255) NOT NULL,
    professional_id      UUID         NOT NULL,
    establishment_id     UUID         NOT NULL,
    professional_rating  SMALLINT     CHECK (professional_rating BETWEEN 1 AND 5),
    establishment_rating SMALLINT     CHECK (establishment_rating BETWEEN 1 AND 5),
    comment              TEXT,
    created_at           TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT chk_at_least_one_rating
        CHECK (professional_rating IS NOT NULL OR establishment_rating IS NOT NULL)
);

-- Unicidade por booking (evita avaliação duplicada)
CREATE UNIQUE INDEX uk_review_booking ON tb_reviews (booking_id);

-- Performance: listagem por profissional
CREATE INDEX idx_review_professional ON tb_reviews (professional_id, created_at DESC);

-- Performance: listagem por estabelecimento
CREATE INDEX idx_review_establishment ON tb_reviews (establishment_id, created_at DESC);
```

### 8.2 Banco dedicado

O `review-service` usa **`review_db`** — banco próprio no PostgreSQL compartilhado. Adicionado ao `infra/init-scripts/init.sql`:

```sql
CREATE DATABASE review_db;
```

---

## 9. Estrutura do Projeto

Segue o padrão Clean / Hexagonal Architecture dos demais serviços:

```
review-service/
├── src/main/java/com/bookinghub/review/
│   ├── ReviewApplication.java
│   ├── core/
│   │   ├── domain/
│   │   │   ├── Review.java
│   │   │   └── EligibleBooking.java
│   │   ├── usecases/
│   │   │   ├── ConsumeBookingCompletedUseCase.java
│   │   │   ├── CreateReviewUseCase.java
│   │   │   ├── GetReviewsByProfessionalUseCase.java
│   │   │   ├── GetReviewsByEstablishmentUseCase.java
│   │   │   └── GetReviewByBookingUseCase.java
│   │   ├── ports/
│   │   │   ├── ReviewRepository.java
│   │   │   ├── EligibleBookingRepository.java
│   │   │   └── ReviewEventPublisher.java
│   │   └── exceptions/
│   │       ├── ReviewNotFoundException.java
│   │       ├── BookingNotEligibleException.java   ← 422
│   │       ├── ReviewAlreadyExistsException.java  ← 409
│   │       └── ForbiddenException.java            ← 403
│   └── infrastructure/
│       ├── adapters/
│       │   ├── in/
│       │   │   ├── rest/
│       │   │   │   ├── ReviewController.java
│       │   │   │   ├── dto/
│       │   │   │   │   ├── CreateReviewRequest.java
│       │   │   │   │   ├── ReviewResponse.java
│       │   │   │   │   ├── ReviewSummary.java
│       │   │   │   │   └── RatingStatsResponse.java
│       │   │   │   └── handler/
│       │   │   │       └── GlobalExceptionHandler.java
│       │   │   └── messaging/
│       │   │       └── BookingCompletedListener.java
│       │   └── out/
│       │       ├── database/
│       │       │   ├── ReviewEntity.java
│       │       │   ├── EligibleBookingEntity.java
│       │       │   ├── JpaReviewRepository.java
│       │       │   ├── JpaEligibleBookingRepository.java
│       │       │   ├── PostgresReviewRepositoryAdapter.java
│       │       │   └── PostgresEligibleBookingRepositoryAdapter.java
│       │       └── messaging/
│       │           └── RabbitMQReviewEventPublisher.java
│       └── configuration/
│           ├── BeanConfig.java
│           ├── RabbitMQConfig.java
│           └── OpenApiConfig.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-docker.yml
│   ├── application-test.yml
│   └── db/migration/
│       └── V1__create_review_tables.sql
└── src/test/
    ├── java/
    │   ├── core/usecases/           ← testes unitários (TDD)
    │   ├── infrastructure/adapters/ ← testes de integração
    │   └── bdd/                     ← Cucumber steps
    └── resources/features/
        ├── create_review.feature
        ├── list_reviews.feature
        └── review_stats.feature
```

---

## 10. Estratégia de Testes

### 10.1 Testes Unitários (TDD — JaCoCo ≥ 80%)

| Classe | Cenários |
|--------|----------|
| `CreateReviewUseCase` | Criação com sucesso, booking inelegível (422), booking alheio (403), double review (409), rating inválido (400) |
| `ConsumeBookingCompletedUseCase` | Persiste EligibleBooking, idempotência (evento duplicado não cria duplicata) |
| `GetReviewsByProfessionalUseCase` | Lista vazia, lista com itens, paginação |
| `GetReviewByBookingUseCase` | Encontrado, não encontrado (404), acesso por não-dono (403) |

### 10.2 BDD / Cucumber

```gherkin
Feature: Criar avaliação

  Scenario: Cliente avalia profissional após serviço concluído
    Given existe um booking concluído do cliente para o profissional
    When o cliente submete uma avaliação com nota 5 para o profissional e 4 para o estabelecimento
    Then a avaliação é criada com status 201
    And a resposta contém professionalRating 5 e establishmentRating 4

  Scenario: Cliente não pode avaliar booking que não pertence a ele
    Given existe um booking concluído de outro cliente
    When o cliente tenta submeter uma avaliação para esse booking
    Then a resposta tem status 403

  Scenario: Não é possível avaliar o mesmo booking duas vezes
    Given o cliente já avaliou o booking
    When o cliente tenta submeter outra avaliação para o mesmo booking
    Then a resposta tem status 409

  Scenario: Não é possível avaliar sem booking concluído
    Given não existe nenhum booking concluído com esse ID
    When o cliente tenta submeter uma avaliação
    Then a resposta tem status 422

Feature: Consultar avaliações

  Scenario: Usuário público consulta avaliações de um profissional
    Given existem 3 avaliações para o profissional
    When uma requisição GET é feita sem autenticação para /reviews/professional/{id}
    Then a resposta tem status 200 e contém 3 avaliações

  Scenario: Consulta de estatísticas de um profissional sem avaliações
    Given não existem avaliações para o profissional
    When uma requisição GET é feita para /reviews/professional/{id}/stats
    Then a resposta tem status 200 com averageRating null e totalReviews 0
```

### 10.3 Testes de Integração

- `BookingCompletedListenerIntegrationTest` — publica mensagem real no RabbitMQ embedded e verifica que `EligibleBooking` é persistido no H2.
- `CreateReviewFlowTest` — fluxo end-to-end: consumir evento → criar review → consultar por profissional.

---

## 11. Configuração e Deploy

### 11.1 Variáveis de Ambiente

| Variável | Local | Docker |
|----------|-------|--------|
| `DB_HOST` | `localhost` | `postgres` |
| `DB_NAME` | `review_db` | `review_db` |
| `DB_USER` | `admin` | `admin` |
| `DB_PASS` | `admin123` | `admin123` |
| `RABBIT_HOST` | `localhost` | `rabbitmq` |
| `OPENAPI_SERVER_URL` | — | `http://localhost:8080/api/reviews` |

### 11.2 `application.yml` base

```yaml
spring:
  application:
    name: review-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:review_db}
    username: ${DB_USER:admin}
    password: ${DB_PASS:admin123}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    open-in-view: false
  flyway:
    enabled: true
    baseline-on-migrate: true
  rabbitmq:
    host: ${RABBIT_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASSWORD:guest}
  jackson:
    serialization:
      write-dates-as-timestamps: false
server:
  port: 8084
app:
  openapi:
    server-url: ${OPENAPI_SERVER_URL:http://localhost:8080/api/reviews}
```

### 11.3 Adição ao `docker-compose.yml`

```yaml
review-service:
  build:
    context: .
    dockerfile: review-service/Dockerfile
  container_name: bw-review-service
  ports:
    - "8084:8084"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - DB_HOST=postgres
    - DB_NAME=review_db
    - DB_USER=admin
    - DB_PASS=admin123
    - RABBIT_HOST=rabbitmq
    - OPENAPI_SERVER_URL=http://localhost:8080/api/reviews
  networks:
    - bw-network
  depends_on:
    postgres:
      condition: service_healthy
    rabbitmq:
      condition: service_healthy
    booking-service:
      condition: service_started
```

### 11.4 Adição ao gateway

**`application-docker.yml` e `application-local.yml`:**
```yaml
- id: review-service-route
  uri: ${REVIEW_SERVICE_URI:http://review-service:8084}
  predicates:
    - Path=/api/reviews/**
  filters:
    - JwtAuthFilter
    - RewritePath=/api/(?<remaining>.*), /${remaining}
```

> Nota: usar `RewritePath=/api/(?<remaining>.*), /${remaining}` para preservar o prefixo `/reviews/` — mesmo padrão adotado para o `booking-service`.

---

## 12. Decisões Técnicas e Trade-offs

| Decisão | Escolha | Alternativa descartada | Motivo |
|---------|---------|------------------------|--------|
| Validação de elegibilidade | Tabela local `tb_eligible_bookings` alimentada por evento | Chamada REST síncrona ao booking-service | Sem acoplamento temporal; resiliente a falhas do booking-service |
| Duplo alvo (profissional + estabelecimento) | Campos opcionais no mesmo `Review` | Duas entidades separadas | Simplicidade de submit; uma review por booking é o contrato natural |
| Cálculo de média | Agregação SQL on-the-fly (`AVG`) | Tabela de agregados mantida por trigger | Sem complexidade extra para o volume esperado na fase 3; pode ser cacheado depois |
| Constraint de ao menos 1 rating | `CHECK` no banco + validação no use case | Apenas validação no use case | Defense-in-depth; banco garante integridade mesmo via scripts diretos |
| Idempotência no listener | `ON CONFLICT DO NOTHING` no INSERT de `EligibleBooking` | Verificar antes de inserir | Evita race condition em re-delivery de mensagem sem overhead de SELECT + INSERT |

---

## 13. Fluxo Completo

```
booking-service             RabbitMQ             review-service              Cliente
      │                        │                        │                       │
      │ PATCH /{id}/complete   │                        │                       │
      │ ──────────────────────▶│                        │                       │
      │                        │── booking.completed ──▶│                       │
      │                        │                        │ INSERT tb_eligible_bookings
      │                        │                        │                       │
      │                        │                        │◀── POST /reviews ─────│
      │                        │                        │    {bookingId, rating} │
      │                        │                        │                       │
      │                        │                        │ 1. Busca EligibleBooking
      │                        │                        │ 2. Valida clientId
      │                        │                        │ 3. Verifica duplicata
      │                        │                        │ 4. INSERT tb_reviews
      │                        │                        │ 5. Publica review.created
      │                        │                        │──────────────────────▶│
      │                        │                        │    201 ReviewResponse  │
```

---

## 14. Ordem de Implementação Sugerida

1. **Setup do módulo** — `pom.xml`, `ReviewApplication`, profiles yml, Dockerfile
2. **Banco** — `init.sql` (`review_db`), Flyway V1 migration
3. **Domínio** — `Review`, `EligibleBooking`, exceptions (TDD first)
4. **Ports** — `ReviewRepository`, `EligibleBookingRepository`, `ReviewEventPublisher`
5. **Use Cases** — `ConsumeBookingCompletedUseCase`, `CreateReviewUseCase`
6. **DB Adapters** — entities, JPA repositories, Postgres adapters
7. **RabbitMQ Listener** — `BookingCompletedListener` (adapter de entrada)
8. **Use Cases de consulta** — `GetReviewsByProfessionalUseCase`, `GetReviewsByEstablishmentUseCase`, `GetReviewByBookingUseCase`
9. **Controller + DTOs** — `ReviewController`, respostas paginadas
10. **BeanConfig** — wire use cases e adapters
11. **OpenAPI config** — Swagger com server URL do gateway
12. **Gateway update** — rota `review-service-route`
13. **docker-compose** — adicionar `review-service`
14. **Testes de integração** — listener + flow end-to-end
15. **BDD** — Cucumber features
