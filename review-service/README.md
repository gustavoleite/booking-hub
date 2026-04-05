# Review Service

Microsserviço responsável por coletar e expor **avaliações e comentários** de clientes sobre profissionais e estabelecimentos no **Booking Hub**. É acionado de forma assíncrona pelo evento `booking.completed` e garante que apenas clientes com atendimentos concluídos possam avaliar.

---

## Responsabilidades

- Consumir eventos `booking.completed` do RabbitMQ e registrar bookings elegíveis para avaliação
- Validar elegibilidade antes de aceitar uma nova review (booking concluído + cliente correto + sem avaliação prévia)
- Persistir reviews com rating de profissional e/ou estabelecimento (1–5 estrelas)
- Expor listagens públicas de reviews por profissional e por estabelecimento
- Calcular e expor estatísticas de rating (média + total) para profissionais e estabelecimentos
- Publicar evento `review.created` no exchange `review.events` para consumo futuro pelo search-service

---

## Stack

| Camada       | Tecnologia                              |
|--------------|-----------------------------------------|
| Framework    | Spring Boot 3.2 / Java 21               |
| Persistência | MongoDB 7 + Spring Data MongoDB         |
| Mensageria   | RabbitMQ (Spring AMQP)                  |
| Documentação | SpringDoc OpenAPI 2 (Swagger UI)        |
| Testes       | JUnit 5, Mockito, Cucumber + REST Assured + Flapdoodle Embedded MongoDB |

---

## Arquitetura Interna (Clean / Hexagonal)

```
core/
  domain/         Review, EligibleBooking
  usecases/       ConsumeBookingCompleted, CreateReview, GetReviewsByProfessional,
                  GetReviewsByEstablishment, GetReviewByBooking
  ports/          ReviewRepository, EligibleBookingRepository, ReviewEventPublisher
  exceptions/     ReviewNotFoundException, BookingNotEligibleException,
                  ReviewAlreadyExistsException, ForbiddenReviewAccessException,
                  InvalidReviewException

application/
  dto/            CreateReviewRequest, ReviewResponse, ReviewSummary,
                  ReviewListResponse, RatingStatsResponse

infrastructure/
  adapters/in/rest/         ReviewController, GlobalExceptionHandler
  adapters/in/messaging/    BookingCompletedListener, BookingCompletedEvent
  adapters/out/database/    JpaReviewRepository, JpaEligibleBookingRepository,
                            PostgresReviewRepositoryAdapter,
                            PostgresEligibleBookingRepositoryAdapter,
                            ReviewEntity, EligibleBookingEntity
  adapters/out/messaging/   RabbitMQReviewEventPublisher, ReviewEventPayload
  configuration/            BeanConfig, RabbitMQConfig, OpenApiConfig
```

---

## Endpoints

Base path via API Gateway: `/api/reviews`
Porta local direta: `8084`

| Método | Path                                       | Auth            | Descrição                                      |
|--------|--------------------------------------------|-----------------|------------------------------------------------|
| `POST` | `/reviews`                                 | `ROLE_CLIENT`   | Submeter avaliação após booking concluído      |
| `GET`  | `/reviews/professional/{professionalId}`   | Público         | Listar reviews de um profissional              |
| `GET`  | `/reviews/professional/{professionalId}/stats` | Público     | Média e total de avaliações do profissional    |
| `GET`  | `/reviews/establishment/{establishmentId}` | Público         | Listar reviews de um estabelecimento           |
| `GET`  | `/reviews/establishment/{establishmentId}/stats` | Público   | Média e total de avaliações do estabelecimento |
| `GET`  | `/reviews/booking/{bookingId}`             | CLIENT/PROF/OWNER | Review de um agendamento específico          |

### POST /reviews

Header obrigatório: `X-User-Id: {clientId}`

```json
{
  "bookingId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "professionalRating": 5,
  "establishmentRating": 4,
  "comment": "Atendimento excelente, ambiente muito agradável."
}
```

> Ao menos um dos campos `professionalRating` ou `establishmentRating` deve estar presente. Ambos são opcionais individualmente.

Resposta `201`:
```json
{
  "id": "uuid",
  "bookingId": "uuid",
  "clientId": "uuid",
  "professionalId": "uuid",
  "establishmentId": "uuid",
  "professionalRating": 5,
  "establishmentRating": 4,
  "comment": "Atendimento excelente, ambiente muito agradável.",
  "createdAt": "2026-04-04T18:00:00"
}
```

### GET /reviews/professional/{id}/stats

```json
{
  "subjectId": "uuid",
  "averageRating": 4.7,
  "totalReviews": 42
}
```

### GET /reviews/professional/{id}

```json
{
  "reviews": [
    {
      "id": "uuid",
      "professionalRating": 5,
      "establishmentRating": 4,
      "comment": "Ótimo atendimento!",
      "createdAt": "2026-04-04T18:00:00"
    }
  ],
  "averageRating": 4.7,
  "totalReviews": 1
}
```

---

## Erros

| HTTP | Exceção                    | Quando                                           |
|------|----------------------------|--------------------------------------------------|
| 400  | `InvalidReviewException`   | Nenhum rating fornecido ou valor fora de 1–5     |
| 403  | `ForbiddenReviewAccessException` | Booking pertence a outro cliente           |
| 404  | `ReviewNotFoundException`  | Review não encontrada para o booking             |
| 409  | `ReviewAlreadyExistsException` | Booking já foi avaliado                      |
| 422  | `BookingNotEligibleException` | Booking não existe ou não foi concluído        |

---

## Fluxo de Elegibilidade

O review-service **não chama o booking-service de forma síncrona**. Em vez disso, consome eventos assíncronos:

```
booking-service          RabbitMQ              review-service
     │                      │                       │
     │── booking.completed ─►│──[listener]──────────►│
     │                      │                       │ INSERT tb_eligible_bookings
     │                      │                       │   (bookingId, clientId,
     │                      │                       │    professionalId,
     │                      │                       │    establishmentId)
     │                      │                       │
     │                      │      Cliente           │
     │                      │◄── POST /reviews ──────│
     │                      │                       │ 1. Busca tb_eligible_bookings
     │                      │                       │ 2. Valida clientId
     │                      │                       │ 3. Verifica duplicata
     │                      │                       │ 4. INSERT tb_reviews
     │                      │                       │ 5. Publica review.created
```

O listener é **idempotente**: reentregas do mesmo evento não geram duplicatas.

---

## Banco de Dados

Banco dedicado: `review_db` (MongoDB)

MongoDB foi escolhido pela **flexibilidade de esquema** na persistência de avaliações em texto livre — o campo `comment` pode evoluir para estruturas mais ricas (tags, respostas, mídia) sem migrações.

### Coleções

**`eligible_bookings`** — alimentada via evento `booking.completed`
```json
{
  "_id": "uuid-do-booking",
  "clientId": "uuid-do-cliente",
  "professionalId": "uuid-do-profissional",
  "establishmentId": "uuid-do-estabelecimento",
  "completedAt": "2026-04-04T18:00:00"
}
```

**`reviews`** — avaliações submetidas pelos clientes
```json
{
  "_id": "uuid-da-review",
  "bookingId": "uuid-do-booking",
  "clientId": "uuid-do-cliente",
  "professionalId": "uuid-do-profissional",
  "establishmentId": "uuid-do-estabelecimento",
  "professionalRating": 5,
  "establishmentRating": 4,
  "comment": "Atendimento excelente!",
  "createdAt": "2026-04-04T18:00:00"
}
```

Índices criados automaticamente pelo Spring Data MongoDB:
- `reviews.bookingId` — unique
- `reviews.professionalId`
- `reviews.establishmentId`

---

## Eventos RabbitMQ

### Consumido

Exchange: `booking.events` | Queue: `review.booking.completed`

| Routing Key        | Origem           | Ação                                    |
|--------------------|------------------|-----------------------------------------|
| `booking.completed`| booking-service  | Registra booking elegível para avaliação |

### Publicado

Exchange: `review.events` (topic, durable)

| Routing Key      | Quando                  | Consumidor futuro        |
|------------------|-------------------------|--------------------------|
| `review.created` | Review submetida com sucesso | search-service (índice de avaliação) |

Payload `review.created`:
```json
{
  "reviewId": "uuid",
  "bookingId": "uuid",
  "clientId": "uuid",
  "professionalId": "uuid",
  "establishmentId": "uuid",
  "professionalRating": 5,
  "establishmentRating": 4,
  "occurredAt": "2026-04-04T18:00:00"
}
```

---

## Configuração

### Variáveis de ambiente (Docker)

| Variável             | Descrição                  | Default (local)              |
|----------------------|----------------------------|------------------------------|
| `MONGO_HOST`         | Host do MongoDB             | `localhost`                  |
| `MONGO_DB`           | Nome do banco               | `review_db`                  |
| `RABBIT_HOST`        | Host do RabbitMQ            | `localhost`                  |
| `RABBITMQ_PORT`      | Porta AMQP                  | `5672`                       |
| `RABBITMQ_USER`      | Usuário RabbitMQ            | `guest`                      |
| `RABBITMQ_PASSWORD`  | Senha RabbitMQ              | `guest`                      |
| `OPENAPI_SERVER_URL` | URL do servidor no Swagger  | `http://localhost:8080/api/reviews` |

### Pré-requisito local

MongoDB rodando em `localhost:27017` (sem autenticação por padrão). O banco `review_db` e as coleções são criados automaticamente pelo Spring Data MongoDB na primeira inserção.

---

## Executar Localmente

```bash
# Sem perfil (usa defaults do application.yml)
mvn spring-boot:run

# Com perfil explícito
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Swagger UI disponível em: http://localhost:8084/swagger-ui.html

---

## Testes

```bash
# Todos os testes
mvn test

# Apenas testes unitários (sem Spring context)
mvn test -Dtest="ReviewTest,CreateReviewUseCaseTest,ConsumeBookingCompletedUseCaseTest"

# Apenas BDD (Spring context + H2)
mvn test -Dtest="CucumberTest"

# Com relatório de cobertura JaCoCo
mvn verify
```

Cobertura mínima: **80% de linhas**. Relatório em `target/site/jacoco/index.html`.

### Suíte de testes

| Classe                               | Tipo | Cenários                                                     |
|--------------------------------------|------|--------------------------------------------------------------|
| `ReviewTest`                         | Unit | Criação com ambos ratings, só profissional, sem rating, fora do range |
| `CreateReviewUseCaseTest`            | Unit | Happy path, booking inelegível, cliente errado, duplicata    |
| `ConsumeBookingCompletedUseCaseTest` | Unit | Persiste elegível, idempotência em reentrega                 |
| `GetReviewsByProfessionalUseCaseTest`| Unit | Com reviews, sem professionalRating, sem reviews             |
| `GetReviewsByEstablishmentUseCaseTest`| Unit | Com reviews, sem reviews                                    |
| `GetReviewByBookingUseCaseTest`      | Unit | Cliente dono, profissional, owner, outro cliente (403), não encontrado |
| `CucumberTest` — `create_review`     | BDD  | Submit ambos ratings, só profissional, outro cliente (403), inelegível (422), duplicata (409), sem rating (400) |
| `CucumberTest` — `list_reviews`      | BDD  | Listar por profissional, por estabelecimento, por booking, acesso negado, não encontrado |
| `CucumberTest` — `review_stats`      | BDD  | Stats sem reviews (null avg), stats com review              |
