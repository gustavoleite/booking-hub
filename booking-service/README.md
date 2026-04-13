# Booking Service

Microsserviço responsável pelo ciclo de vida completo de **agendamentos e avaliações** no **Booking Hub**. Aplica regras rígidas de negócio e concorrência para evitar *double-booking*, consulta o Catalog Service para validar disponibilidade, publica eventos de domínio no RabbitMQ e gerencia o fluxo de avaliações pós-atendimento diretamente no mesmo banco PostgreSQL.

---

## Responsabilidades

### Agendamentos
- Criar, consultar, cancelar e finalizar agendamentos
- Validar disponibilidade do profissional consultando o Catalog Service via REST
- Prevenir agendamentos duplicados com índice único parcial no PostgreSQL
- Expor slots disponíveis publicamente (sem autenticação)
- Publicar eventos `BookingCreated`, `BookingCancelled`, `BookingCompleted` no exchange `booking.events`

### Avaliações
- Ao finalizar um booking, registrar o booking como **elegível para avaliação** diretamente no banco (tabela `tb_eligible_bookings`) — sem RabbitMQ intermediário
- Receber avaliações de clientes (`POST /reviews`) e validar elegibilidade
- Publicar evento `ReviewCreated` no exchange `review.events` (consumido pelo search-service para atualizar `averageRating`)
- Consultar avaliações por profissional, por estabelecimento e por booking

---

## Stack

| Camada          | Tecnologia                              |
|-----------------|-----------------------------------------|
| Framework       | Spring Boot 3.2 / Java 21               |
| Persistência    | PostgreSQL + Spring Data JPA + Flyway   |
| Mensageria      | RabbitMQ (Spring AMQP)                  |
| HTTP Client     | Spring `RestClient` (catalog sync)      |
| Documentação    | SpringDoc OpenAPI 2 (Swagger UI)        |
| Testes          | JUnit 5, Mockito, Cucumber + REST Assured |

---

## Arquitetura Interna (Clean / Hexagonal)

```
core/
  domain/         Booking, BookingStatus, DaySchedule, ScheduleInfo
                  Review, EligibleBooking
  usecases/       CreateBookingUseCase, CancelBookingUseCase, CompleteBookingUseCase,
                  GetAvailableSlotsUseCase, GetBookingDetailsUseCase, ListClientBookingsUseCase,
                  ListEstablishmentBookingsUseCase, ListProfessionalAgendaUseCase, MarkNoShowUseCase
                  ConsumeBookingCompletedUseCase, CreateReviewUseCase,
                  GetReviewsByProfessionalUseCase, GetReviewsByEstablishmentUseCase, GetReviewByBookingUseCase
  ports/          BookingRepository, CatalogServiceClient, BookingEventPublisher
                  ReviewRepository, EligibleBookingRepository, ReviewEventPublisher
  exceptions/     BookingNotFoundException, SlotUnavailableException, BookingStatusException,
                  ForbiddenBookingAccessException, CatalogServiceException
                  BookingNotEligibleException, ForbiddenReviewAccessException, InvalidReviewException,
                  ReviewAlreadyExistsException, ReviewNotFoundException

application/
  dto/            CreateBookingRequest, BookingResponse, AvailabilityResponse
                  CreateReviewRequest, ReviewResponse, ReviewSummary, ReviewListResponse, RatingStatsResponse

infrastructure/
  adapters/in/rest/         BookingController, AvailabilityController, ReviewController, GlobalExceptionHandler
  adapters/out/database/    JpaBookingRepository, PostgresBookingRepositoryAdapter, BookingEntity
                            JpaReviewRepository, PostgresReviewRepositoryAdapter, ReviewJpaEntity
                            JpaEligibleBookingRepository, PostgresEligibleBookingRepositoryAdapter, EligibleBookingJpaEntity
  adapters/out/messaging/   RabbitMQBookingEventPublisher, RabbitMQReviewEventPublisher
  adapters/out/catalog/     CatalogServiceRestClient, ScheduleResponse
  configuration/            BeanConfig, RabbitMQConfig, RestClientConfig, OpenApiConfig
```

---

## Endpoints

Base path via API Gateway: `/api/bookings`  
Porta local direta: `8082`

| Método  | Path                           | Auth            | Descrição                                      |
|---------|--------------------------------|-----------------|------------------------------------------------|
| `GET`   | `/bookings/availability`       | Nenhuma         | Slots disponíveis para um profissional/serviço |
| `POST`  | `/bookings`                    | `ROLE_CLIENT`   | Criar agendamento                              |
| `GET`   | `/bookings/me`                 | `ROLE_CLIENT`   | Listar meus agendamentos                       |
| `GET`   | `/bookings/{id}`               | Qualquer role   | Detalhes de um agendamento                     |
| `PATCH` | `/bookings/{id}/cancel`        | CLIENT ou OWNER | Cancelar agendamento                           |
| `PATCH` | `/bookings/{id}/complete`      | PROF ou OWNER   | Marcar como concluído                          |
| `PATCH` | `/bookings/{id}/no-show`       | PROF ou OWNER   | Marcar como não compareceu                     |
| `GET`   | `/bookings/professional`       | `ROLE_PROFESSIONAL` | Minha agenda (por professional id no token) |
| `GET`   | `/bookings/establishment/{id}` | PROF ou OWNER   | Todos os agendamentos do estabelecimento       |

### Endpoints de Avaliação

Base path via API Gateway: `/api/reviews`

| Método | Path                                      | Auth            | Descrição                                         |
|--------|-------------------------------------------|-----------------|---------------------------------------------------|
| `POST` | `/reviews`                                | `ROLE_CLIENT`   | Criar avaliação para um booking completado        |
| `GET`  | `/reviews/professional/{id}`              | Pública         | Listar avaliações de um profissional              |
| `GET`  | `/reviews/professional/{id}/stats`        | Pública         | Média e total de avaliações do profissional       |
| `GET`  | `/reviews/establishment/{id}`             | Pública         | Listar avaliações de um estabelecimento           |
| `GET`  | `/reviews/establishment/{id}/stats`       | Pública         | Média e total de avaliações do estabelecimento    |
| `GET`  | `/reviews/booking/{id}`                   | CLIENT/PROF/OWNER | Avaliação de um booking específico              |

### GET /bookings/availability

Query params obrigatórios:

| Param            | Tipo   | Exemplo                                |
|------------------|--------|----------------------------------------|
| `establishmentId`| UUID   | `3fa85f64-5717-4562-b3fc-2c963f66afa6` |
| `professionalId` | UUID   | `3fa85f64-5717-4562-b3fc-2c963f66afa7` |
| `serviceId`      | UUID   | `3fa85f64-5717-4562-b3fc-2c963f66afa8` |
| `date`           | date   | `2026-04-07`                           |

Resposta `200`:
```json
{
  "establishmentId": "...",
  "professionalId": "...",
  "providedServiceId": "...",
  "durationMinutes": 60,
  "price": 120.00,
  "availableSlots": [
    "2026-04-07T09:00:00",
    "2026-04-07T10:00:00"
  ]
}
```

### POST /bookings

```json
{
  "professionalId": "uuid",
  "establishmentId": "uuid",
  "providedServiceId": "uuid",
  "startDatetime": "2026-04-07T10:00:00",
  "notes": "Prefiro corte mais curto nas laterais"
}
```

Resposta `201`:
```json
{
  "id": "uuid",
  "clientId": "uuid",
  "status": "CONFIRMED",
  "price": 120.00,
  "durationMinutes": 60,
  "startDatetime": "2026-04-07T10:00:00",
  "endDatetime": "2026-04-07T11:00:00",
  "createdAt": "2026-04-03T12:00:00"
}
```

---

## Ciclo de Vida do Agendamento

```
                    ┌──────────────┐
                    │  CONFIRMED   │◄── POST /bookings
                    └──────┬───────┘
           ┌───────────────┼──────────────────┐
           ▼               ▼                  ▼
     ┌──────────┐   ┌───────────┐    ┌─────────────┐
     │CANCELLED │   │ COMPLETED │    │   NO_SHOW   │
     └──────────┘   └───────────┘    └─────────────┘
```

| Transição     | Quem pode             | Endpoint                    |
|---------------|-----------------------|-----------------------------|
| → CANCELLED   | CLIENT (próprio) ou OWNER | `PATCH /{id}/cancel`    |
| → COMPLETED   | PROFESSIONAL ou OWNER | `PATCH /{id}/complete`      |
| → NO_SHOW     | PROFESSIONAL ou OWNER | `PATCH /{id}/no-show`       |

---

## Anti-Double-Booking

Duas camadas de proteção:

1. **Verificação na camada de aplicação** (`CreateBookingUseCase`) — retorna `409 Conflict` com mensagem amigável antes de tocar o banco.
2. **Índice único parcial no PostgreSQL** — garante consistência mesmo sob concorrência:

```sql
CREATE UNIQUE INDEX uk_booking_active_slot
  ON tb_bookings (professional_id, start_datetime)
  WHERE status NOT IN ('CANCELLED', 'NO_SHOW');
```

### Tabelas de Avaliação (V2)

- **`tb_eligible_bookings`** — bookings completados que podem ser avaliados. Chave primária é o `booking_id`.
- **`tb_reviews`** — avaliações submetidas. Cada booking pode ter no máximo uma avaliação (constraint `UNIQUE` em `booking_id`).

---

## Eventos RabbitMQ

### Exchange `booking.events` (topic, durable)

| Routing Key         | Quando é publicado                 | Consumido por          |
|---------------------|------------------------------------|------------------------|
| `booking.created`   | Agendamento criado com sucesso     | notification-service   |
| `booking.cancelled` | Agendamento cancelado              | notification-service   |
| `booking.completed` | Atendimento marcado como concluído | notification-service   |

O payload é **enriquecido**: o `clientEmail` vem do header `X-User-Email` (injetado pelo gateway a partir do claim JWT) e o `professionalEmail` é buscado via `GET /internal/users/{id}/email` no auth-service no momento da criação do booking.

Payload `booking.events`:
```json
{
  "bookingId": "uuid",
  "clientId": "uuid",
  "clientEmail": "cliente@email.com",
  "professionalId": "uuid",
  "professionalEmail": "prof@salon.com",
  "establishmentId": "uuid",
  "establishmentName": "Salão da Maria",
  "providedServiceId": "uuid",
  "serviceName": "Corte Feminino",
  "startDatetime": "2026-04-07T10:00:00",
  "endDatetime": "2026-04-07T11:00:00",
  "price": 120.00,
  "durationMinutes": 60,
  "status": "COMPLETED",
  "occurredAt": "2026-04-03T12:00:00"
}
```

### Exchange `review.events` (topic, durable)

| Routing Key      | Quando é publicado             | Consumido por              |
|------------------|--------------------------------|----------------------------|
| `review.created` | Avaliação criada pelo cliente  | search-service (atualiza `averageRating`) |

Payload `review.events`:
```json
{
  "reviewId": "uuid",
  "bookingId": "uuid",
  "clientId": "uuid",
  "professionalId": "uuid",
  "establishmentId": "uuid",
  "professionalRating": 5,
  "establishmentRating": 4,
  "occurredAt": "2026-04-03T13:00:00"
}
```

### Fluxo interno (sem mensageria)

Ao chamar `PATCH /bookings/{id}/complete`, o `CompleteBookingUseCase` invoca `ConsumeBookingCompletedUseCase` diretamente, gravando o booking elegível em `tb_eligible_bookings` na mesma transação — sem depender de um consumer RabbitMQ externo.

---

## Configuração

### Variáveis de ambiente (Docker)

| Variável              | Descrição                         | Default (local)         |
|-----------------------|-----------------------------------|-------------------------|
| `DB_HOST`             | Host do PostgreSQL                | `localhost`             |
| `DB_PORT`             | Porta do PostgreSQL               | `5432`                  |
| `DB_NAME`             | Nome do banco                     | `booking_db`            |
| `DB_USER`             | Usuário do banco                  | `admin`                 |
| `DB_PASS`             | Senha do banco                    | `admin123`              |
| `RABBIT_HOST`         | Host do RabbitMQ                  | `localhost`             |
| `RABBITMQ_PORT`       | Porta AMQP                        | `5672`                  |
| `RABBITMQ_USER`       | Usuário RabbitMQ                  | `guest`                 |
| `RABBITMQ_PASSWORD`   | Senha RabbitMQ                    | `guest`                 |
| `CATALOG_SERVICE_URI` | URL base do Catalog Service       | `http://localhost:8083` |
| `OPENAPI_SERVER_URL`  | URL do servidor no Swagger        | `http://localhost:8080/api/bookings` |

### Pré-requisito local

Criar o banco antes de iniciar:
```sql
CREATE DATABASE booking_db;
```

---

## Executar Localmente

```bash
# Sem perfil (usa defaults do application.yml — localhost para tudo)
mvn spring-boot:run

# Com perfil explícito
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Swagger UI disponível em: http://localhost:8082/swagger-ui.html

---

## Testes

```bash
# Todos os testes
mvn test

# Apenas testes unitários (sem Spring context)
mvn test -Dtest="BookingTest,CreateBookingUseCaseTest,CancelBookingUseCaseTest,GetAvailableSlotsUseCaseTest"

# Apenas BDD (Spring context + H2)
mvn test -Dtest="CucumberTest"

# Com relatório de cobertura JaCoCo
mvn verify
```

### Cobertura

Cobertura mínima: **80% de linhas**. Relatório gerado em `target/site/jacoco/index.html`.

Excluídos da contagem: entidades JPA (`*Entity`), DTOs de request/response.

### Suíte de testes

| Classe                             | Tipo      | Cenários                                                |
|------------------------------------|-----------|---------------------------------------------------------|
| `BookingTest`                      | Unit      | Criação, transições de status, validações de domínio    |
| `CreateBookingUseCaseTest`         | Unit      | Happy path, slot ocupado, data passada, fora do horário |
| `CancelBookingUseCaseTest`         | Unit      | Cancel por cliente, por owner, acesso negado            |
| `CompleteBookingUseCaseTest`       | Unit      | Complete por profissional/owner, registro de elegível   |
| `GetAvailableSlotsUseCaseTest`     | Unit      | Dia sem agenda, serviço inativo, slots futuros          |
| `CucumberTest`                     | BDD / E2E | Disponibilidade com e sem agenda no dia                 |
