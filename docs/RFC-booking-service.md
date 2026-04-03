# RFC: Booking Service

**Status:** Proposta  
**Data:** 2026-04-03  
**Autor:** Booking Hub Team  

---

## 1. Contexto e Motivação

O `catalog-service` já entrega a base estrutural do sistema: estabelecimentos, profissionais, horários de funcionamento e ofertas de serviço (preço + duração) por afiliação. O próximo passo obrigatório do Tech Challenge é o **`booking-service`** — o núcleo transacional do produto.

O serviço precisa resolver três problemas fundamentais:

1. **Double-booking:** dois clientes tentando agendar o mesmo profissional no mesmo horário simultaneamente.
2. **Disponibilidade calculada:** dado um dia, profissional e serviço, quais slots estão livres?
3. **Ciclo de vida do agendamento:** criação → confirmação → conclusão/cancelamento, com notificações em cada transição.

---

## 2. Requisitos (origem: Tech-Challenge-Fase-3.md)

| # | Requisito |
|---|-----------|
| R1 | Clientes visualizam serviços, disponibilidade de profissionais e fazem agendamentos para datas e horários específicos |
| R2 | O sistema envia confirmações e lembretes automáticos para cliente e profissional |
| R3 | Estabelecimentos gerenciam agendamentos: cancelamentos, não comparecimentos e reagendamentos |
| R4 | O sistema suporta volume elevado de agendamentos simultâneos sem degradação (teste de carga k6) |
| R5 | Integração com calendários externos (Google Calendar / Outlook) para profissionais e clientes |

---

## 3. Domínio

### 3.1 Entidade Principal: `Booking`

```
Booking
├── id:               UUID
├── clientId:         String          ← X-User-Id do JWT (UUID do usuário no auth-service)
├── professionalId:   UUID            ← referência ao catalog-service
├── establishmentId:  UUID            ← referência ao catalog-service
├── providedServiceId: UUID           ← referência ao catalog-service
├── startDatetime:    LocalDateTime   ← slot escolhido pelo cliente
├── endDatetime:      LocalDateTime   ← startDatetime + durationMinutes (calculado)
├── status:           BookingStatus
├── price:            BigDecimal      ← snapshot do preço no momento do agendamento
├── durationMinutes:  int             ← snapshot da duração no momento do agendamento
├── notes:            String          ← observações opcionais do cliente
├── cancelReason:     String
├── createdAt:        LocalDateTime
└── cancelledAt:      LocalDateTime
```

> **Por que snapshots de preço e duração?** O preço pode mudar no catálogo após o agendamento. O booking registra o valor contratado.

### 3.2 Ciclo de Vida: `BookingStatus`

```
                    ┌─────────┐
         POST /booking  │         │
  ──────────────────▶ │ PENDING │
                    │         │
                    └────┬────┘
                         │ (automático ou confirmação manual)
                         ▼
                    ┌───────────┐       ┌───────────┐
                    │ CONFIRMED │──────▶│ COMPLETED │
                    └─────┬─────┘       └───────────┘
                          │
              ┌───────────┴──────────┐
              ▼                      ▼
         ┌──────────┐          ┌──────────┐
         │CANCELLED │          │ NO_SHOW  │
         └──────────┘          └──────────┘
```

| Status | Quem pode transicionar | Evento publicado |
|--------|------------------------|-----------------|
| `PENDING → CONFIRMED` | Sistema (automático ao criar) | `booking.created` |
| `CONFIRMED → COMPLETED` | `ROLE_PROFESSIONAL` ou `ROLE_OWNER` | `booking.completed` |
| `CONFIRMED → CANCELLED` | `ROLE_CLIENT` (até N horas antes), `ROLE_OWNER` | `booking.cancelled` |
| `CONFIRMED → NO_SHOW` | `ROLE_PROFESSIONAL` ou `ROLE_OWNER` | `booking.cancelled` |

> **Decisão de design:** O status inicial é `CONFIRMED` diretamente (sem aprovação manual). Isso cobre o requisito R1 e simplifica o fluxo para a fase 3. Um status `PENDING` pode ser usado em fases futuras para aprovação manual pelo estabelecimento.

---

## 4. Casos de Uso

| Use Case | Actor | Descrição |
|----------|-------|-----------|
| `CreateBookingUseCase` | `ROLE_CLIENT` | Valida slot, previne double-booking, persiste, publica evento |
| `GetAvailableSlotsUseCase` | Público | Calcula slots livres para (profissional, estabelecimento, serviço, data) |
| `CancelBookingUseCase` | `ROLE_CLIENT`, `ROLE_OWNER` | Cancela booking ativo, publica evento |
| `CompleteBookingUseCase` | `ROLE_PROFESSIONAL`, `ROLE_OWNER` | Marca como concluído |
| `MarkNoShowUseCase` | `ROLE_PROFESSIONAL`, `ROLE_OWNER` | Marca não comparecimento |
| `ListClientBookingsUseCase` | `ROLE_CLIENT` | Lista agendamentos do cliente autenticado |
| `ListProfessionalAgendaUseCase` | `ROLE_PROFESSIONAL` | Lista agenda do profissional autenticado |
| `ListEstablishmentBookingsUseCase` | `ROLE_OWNER` | Lista agendamentos de um estabelecimento |
| `GetBookingDetailsUseCase` | Cliente, Prof., Owner do booking | Detalhe de um agendamento específico |

---

## 5. API REST

Prefixo no gateway: `/api/bookings/**` → porta interna `8082`

### 5.1 Endpoints

```
GET  /bookings/availability
     ?professionalId={uuid}
     &establishmentId={uuid}
     &serviceId={uuid}
     &date={yyyy-MM-dd}
     → Lista de slots livres (LocalTime[])
     → Público, sem autenticação

POST /bookings
     Body: { professionalId, establishmentId, serviceId, startDatetime, notes? }
     → Booking criado (201)
     → ROLE_CLIENT

GET  /bookings/me
     ?status={status}&page={n}&size={n}
     → Page<BookingSummary>
     → ROLE_CLIENT

GET  /bookings/professional
     ?date={yyyy-MM-dd}&status={status}
     → List<BookingSummary>
     → ROLE_PROFESSIONAL

GET  /bookings/establishment/{establishmentId}
     ?date={yyyy-MM-dd}&professionalId={uuid}&status={status}
     → List<BookingSummary>
     → ROLE_OWNER

GET  /bookings/{id}
     → BookingDetails
     → Cliente dono, profissional ou owner do estabelecimento

PATCH /bookings/{id}/cancel
      Body: { reason? }
      → 204
      → ROLE_CLIENT (com restrição de antecedência) ou ROLE_OWNER

PATCH /bookings/{id}/complete
      → 204
      → ROLE_PROFESSIONAL ou ROLE_OWNER

PATCH /bookings/{id}/no-show
      → 204
      → ROLE_PROFESSIONAL ou ROLE_OWNER
```

### 5.2 Request/Response de Criação

**Request `POST /bookings`:**
```json
{
  "professionalId": "b6840d12-c231-4dc4-b623-df5b23298f13",
  "establishmentId": "e7e666e0-1589-4ead-b7a5-c8e05795cc48",
  "serviceId": "fbab4956-803d-42fe-87db-e26b39c4a654",
  "startDatetime": "2026-04-10T09:00:00",
  "notes": "Prefiro corte mais curto nas laterais"
}
```

**Response `201 Created`:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "clientId": "26cc6703-b9f2-476d-9d3d-ac6f1dec37b0",
  "professionalId": "b6840d12-c231-4dc4-b623-df5b23298f13",
  "establishmentId": "e7e666e0-1589-4ead-b7a5-c8e05795cc48",
  "serviceId": "fbab4956-803d-42fe-87db-e26b39c4a654",
  "startDatetime": "2026-04-10T09:00:00",
  "endDatetime": "2026-04-10T09:45:00",
  "status": "CONFIRMED",
  "price": 75.00,
  "durationMinutes": 45,
  "notes": "Prefiro corte mais curto nas laterais",
  "createdAt": "2026-04-03T15:30:00"
}
```

**Response `GET /bookings/availability`:**
```json
{
  "date": "2026-04-10",
  "professionalId": "b6840d12-...",
  "establishmentId": "e7e666e0-...",
  "serviceId": "fbab4956-...",
  "durationMinutes": 45,
  "availableSlots": [
    "09:00", "09:45", "10:30", "11:15",
    "14:00", "14:45", "15:30"
  ]
}
```

---

## 6. Estratégia Anti-Double-Booking

Esta é a decisão técnica mais crítica do serviço.

### 6.1 Camada de Banco de Dados (primeira linha de defesa)

```sql
-- Índice único parcial: apenas bookings ativos disputam o slot
CREATE UNIQUE INDEX uk_booking_active_slot
  ON tb_bookings (professional_id, start_datetime)
  WHERE status NOT IN ('CANCELLED', 'NO_SHOW');
```

Este índice garante que **duas transações concorrentes nunca confirmem o mesmo slot**, mesmo em cenários de alta concorrência, porque o PostgreSQL serializa os INSERTs que conflitem nesta chave.

### 6.2 Camada de Aplicação (segunda linha — melhor UX)

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public Booking execute(CreateBookingCommand cmd) {
    // 1. Busca no próprio DB se o slot já está ocupado (SELECT FOR UPDATE)
    boolean slotTaken = bookingRepository.existsActiveBookingForSlot(
        cmd.professionalId(), cmd.startDatetime()
    );
    if (slotTaken) {
        throw new SlotUnavailableException("Horário já reservado");
    }

    // 2. Valida contra o catálogo (slot dentro do horário de trabalho)
    ScheduleInfo schedule = catalogClient.getSchedule(
        cmd.establishmentId(), cmd.professionalId(), cmd.serviceId()
    );
    validateSlotWithinWorkSchedule(cmd.startDatetime(), schedule);

    // 3. Persiste — se houver race condition, o índice único no DB captura
    Booking booking = Booking.create(cmd, schedule);
    return bookingRepository.save(booking);
}
```

A verificação na aplicação captura o caso comum com uma mensagem legível. O índice único no banco é o safety net para o caso raro de race condition entre transações concorrentes.

### 6.3 Por que não SELECT FOR UPDATE?

`SELECT FOR UPDATE` trava a linha e serializa completamente o acesso — eficaz, mas cria contenção severa sob carga. Para slots distintos (99,9% dos casos), a contenção é desnecessária. A combinação **verificação otimista + índice único** tem throughput superior e degrada graciosamente.

---

## 7. Integração com Catalog Service

O `booking-service` chama o `catalog-service` de forma síncrona via REST para **validar e obter dados de snapshot** no momento do agendamento.

### 7.1 Port (Anti-Corruption Layer)

```java
// core/ports/CatalogServiceClient.java
public interface CatalogServiceClient {
    ScheduleInfo getSchedule(UUID establishmentId, UUID professionalId, UUID serviceId);
}

// Dados que o booking precisa:
public record ScheduleInfo(
    boolean isActive,
    BigDecimal price,
    int durationMinutes,
    List<DaySchedule> workSchedule   // dia + startTime + endTime
) {}
```

### 7.2 Adapter (REST client)

O adapter chama:
```
GET http://catalog-service:8083/establishments/{estId}/affiliations/professional/{profId}/schedule?serviceId={svcId}
```

Esse endpoint já existe e retorna exatamente os dados necessários (`price`, `durationMinutes`, `fixedSchedule`).

### 7.3 Resiliência

O client usa `spring-retry` com 2 tentativas e fallback de `503 Service Unavailable` se o catalog-service não responder. O booking-service **não** armazena dados do catálogo localmente — cada agendamento faz uma chamada fresca.

---

## 8. Eventos RabbitMQ

Exchange: `booking.events` (topic, durável)

| Routing Key | Payload | Consumidores futuros |
|-------------|---------|----------------------|
| `booking.created` | `{bookingId, clientId, professionalId, establishmentId, serviceId, startDatetime, endDatetime, price}` | notification-service (email/SMS de confirmação) |
| `booking.cancelled` | `{bookingId, clientId, professionalId, reason, cancelledAt}` | notification-service (aviso de cancelamento) |
| `booking.completed` | `{bookingId, clientId, professionalId, establishmentId, completedAt}` | notification-service, review-service (trigger para avaliação) |

Serialização: JSON via `Jackson2JsonMessageConverter` (mesmo padrão do catalog-service).

---

## 9. Banco de Dados

### 9.1 Schema (Flyway V1)

```sql
CREATE TYPE booking_status AS ENUM (
    'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW'
);

CREATE TABLE tb_bookings (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id           VARCHAR(255)    NOT NULL,
    professional_id     UUID            NOT NULL,
    establishment_id    UUID            NOT NULL,
    provided_service_id UUID            NOT NULL,
    start_datetime      TIMESTAMP       NOT NULL,
    end_datetime        TIMESTAMP       NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'CONFIRMED',
    price               DECIMAL(19, 2)  NOT NULL,
    duration_minutes    INT             NOT NULL,
    notes               TEXT,
    cancel_reason       TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT now(),
    cancelled_at        TIMESTAMP
);

-- Anti-double-booking: slots únicos por profissional (apenas bookings ativos)
CREATE UNIQUE INDEX uk_booking_active_slot
    ON tb_bookings (professional_id, start_datetime)
    WHERE status NOT IN ('CANCELLED', 'NO_SHOW');

-- Performance: consultas de agenda por profissional/data
CREATE INDEX idx_booking_professional_date
    ON tb_bookings (professional_id, start_datetime);

-- Performance: consultas de histórico do cliente
CREATE INDEX idx_booking_client
    ON tb_bookings (client_id, start_datetime DESC);

-- Performance: painel do estabelecimento
CREATE INDEX idx_booking_establishment
    ON tb_bookings (establishment_id, start_datetime);
```

### 9.2 Banco dedicado

O `booking-service` usa **`booking_db`** — banco próprio no PostgreSQL compartilhado. Adicionado ao `infra/init-scripts/init.sql`:

```sql
CREATE DATABASE booking_db;
```

---

## 10. Estrutura do Projeto

Segue exatamente o padrão do `catalog-service` (Clean / Hexagonal Architecture):

```
booking-service/
├── src/main/java/com/bookinghub/booking/
│   ├── BookingApplication.java
│   ├── core/
│   │   ├── domain/
│   │   │   ├── Booking.java
│   │   │   ├── BookingStatus.java
│   │   │   ├── DaySchedule.java
│   │   │   └── ScheduleInfo.java
│   │   ├── usecases/
│   │   │   ├── CreateBookingUseCase.java
│   │   │   ├── GetAvailableSlotsUseCase.java
│   │   │   ├── CancelBookingUseCase.java
│   │   │   ├── CompleteBookingUseCase.java
│   │   │   ├── MarkNoShowUseCase.java
│   │   │   ├── ListClientBookingsUseCase.java
│   │   │   ├── ListProfessionalAgendaUseCase.java
│   │   │   ├── ListEstablishmentBookingsUseCase.java
│   │   │   └── GetBookingDetailsUseCase.java
│   │   ├── ports/
│   │   │   ├── BookingRepository.java
│   │   │   ├── CatalogServiceClient.java
│   │   │   └── BookingEventPublisher.java
│   │   └── exceptions/
│   │       ├── SlotUnavailableException.java
│   │       ├── BookingNotFoundException.java
│   │       └── ForbiddenException.java
│   └── infrastructure/
│       ├── adapters/
│       │   ├── in/rest/
│       │   │   ├── BookingController.java
│       │   │   ├── AvailabilityController.java
│       │   │   ├── dto/
│       │   │   │   ├── CreateBookingRequest.java
│       │   │   │   ├── BookingResponse.java
│       │   │   │   ├── AvailabilityResponse.java
│       │   │   │   └── CancelBookingRequest.java
│       │   │   └── handler/
│       │   │       └── GlobalExceptionHandler.java
│       │   ├── out/
│       │   │   ├── database/
│       │   │   │   ├── BookingEntity.java
│       │   │   │   ├── JpaBookingRepository.java
│       │   │   │   └── PostgresBookingRepositoryAdapter.java
│       │   │   ├── messaging/
│       │   │   │   └── RabbitMQBookingEventPublisher.java
│       │   │   └── catalog/
│       │   │       └── CatalogServiceRestClient.java
│       └── configuration/
│           ├── BeanConfig.java
│           ├── RabbitMQConfig.java
│           └── OpenApiConfig.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-local.yml
│   ├── application-docker.yml
│   └── db/migration/
│       └── V1__create_booking_tables.sql
└── src/test/
    ├── java/...
    │   ├── core/usecases/          ← testes unitários (TDD)
    │   ├── infrastructure/adapters/← testes de integração (Testcontainers)
    │   └── bdd/                    ← Cucumber steps
    └── resources/features/
        ├── create_booking.feature
        ├── availability.feature
        └── cancel_booking.feature
```

---

## 11. Estratégia de Testes

### 11.1 Testes Unitários (TDD — 80%+ cobertura)

Todos os `UseCases` são testados em isolamento com Mockito:
- `CreateBookingUseCase`: slot disponível, slot ocupado (double-booking), slot fora do horário, profissional inativo
- `GetAvailableSlotsUseCase`: cálculo correto de slots, dia sem horário de trabalho, todos os slots cheios
- `CancelBookingUseCase`: cancelamento próprio, tentativa de cancelar booking alheio

### 11.2 Testes de Integração (Testcontainers)

```java
@SpringBootTest
@Testcontainers
class CreateBookingConcurrencyTest {
    // 10 threads tentam reservar o mesmo slot simultaneamente
    // Apenas 1 deve ter sucesso, as outras devem receber SlotUnavailableException
}
```

O teste de concorrência é a prova formal da estratégia anti-double-booking.

### 11.3 BDD / Cucumber

```gherkin
Feature: Agendamento de serviço

  Scenario: Cliente agenda com sucesso
    Given um profissional ativo com horário disponível na segunda-feira das 09:00 às 18:00
    And o serviço tem duração de 45 minutos
    And não há agendamentos para o profissional às 09:00 do dia 2026-04-13
    When o cliente faz um agendamento para 2026-04-13T09:00:00
    Then o agendamento é criado com status CONFIRMED
    And o evento booking.created é publicado no RabbitMQ

  Scenario: Double-booking é prevenido
    Given já existe um agendamento confirmado para o profissional às 09:00 do dia 2026-04-13
    When outro cliente tenta agendar para o mesmo horário
    Then a requisição falha com status 409 Conflict
    And a mensagem de erro indica "Horário já reservado"
```

### 11.4 Teste de Performance (k6)

```javascript
// scripts/k6/booking_load_test.js
// Simula 100 usuários concorrentes tentando agendar diferentes slots
// Critério de aceitação: p95 < 500ms, 0% de double-bookings
```

---

## 12. Configuração e Deploy

### 12.1 Variáveis de Ambiente

| Variável | Local | Docker |
|----------|-------|--------|
| `DB_HOST` | `localhost` | `postgres` |
| `DB_NAME` | `booking_db` | `booking_db` |
| `DB_USER` | `admin` | `admin` |
| `DB_PASS` | `admin123` | `admin123` |
| `RABBIT_HOST` | `localhost` | `rabbitmq` |
| `CATALOG_SERVICE_URI` | `http://localhost:8083` | `http://catalog-service:8083` |
| `OPENAPI_SERVER_URL` | — | `http://localhost:8080/api/bookings` |

### 12.2 Adições ao `docker-compose.yml`

```yaml
booking-service:
  build:
    context: .
    dockerfile: booking-service/Dockerfile
  container_name: bw-booking-service
  ports:
    - "8082:8082"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - DB_HOST=postgres
    - DB_PORT=5432
    - DB_NAME=booking_db
    - DB_USER=admin
    - DB_PASS=admin123
    - RABBIT_HOST=rabbitmq
    - CATALOG_SERVICE_URI=http://catalog-service:8083
  networks:
    - bw-network
  depends_on:
    postgres:
      condition: service_healthy
    rabbitmq:
      condition: service_healthy
    catalog-service:
      condition: service_started
```

### 12.3 Adição ao `api-gateway/application-local.yml`

```yaml
- id: booking-service-route
  uri: ${BOOKING_SERVICE_URI:http://localhost:8082}
  predicates:
    - Path=/api/bookings/**
  filters:
    - JwtAuthFilter
    - RewritePath=/api/bookings/(?<remaining>.*), /${remaining}
```

### 12.4 Adição ao `api-gateway/application.yml` (Swagger)

```yaml
springdoc:
  swagger-ui:
    urls:
      - name: Booking Service
        url: /api/bookings/v3/api-docs
```

---

## 13. Decisões Técnicas e Trade-offs

| Decisão | Escolha | Alternativa descartada | Motivo |
|---------|---------|------------------------|--------|
| Prevenção de double-booking | Índice único parcial + verificação otimista | `SELECT FOR UPDATE` | Menor contenção sob carga, throughput superior |
| Dados do catálogo | Chamada REST síncrona + snapshot | Cache local do catálogo | Catálogo é a fonte da verdade; snapshot preserva o contrato em caso de mudança de preço |
| Comunicação com catálogo | REST (anti-corruption layer via port/adapter) | Acesso direto ao banco do catálogo | Isolamento de domínios; segue arquitetura existente |
| Status inicial do booking | `CONFIRMED` (sem aprovação manual) | `PENDING` com aprovação | Simplifica o fluxo para a fase 3; `PENDING` pode ser retomado depois |
| Banco de dados | PostgreSQL dedicado (`booking_db`) | MongoDB | Transações ACID críticas para integridade de slots; JOINs eficientes para consultas de agenda |

---

## 14. Fluxo Completo de Agendamento

```
Cliente                   Gateway              Booking Service        Catalog Service        RabbitMQ
  │                          │                       │                       │                  │
  │── GET /availability ────▶│── GET /bookings/ ────▶│                       │                  │
  │                          │   availability        │── GET /schedule ─────▶│                  │
  │                          │                       │◀─ {price, duration, ──│                  │
  │                          │                       │    workSchedule}       │                  │
  │                          │                       │                       │                  │
  │                          │                       │ Calcula slots livres   │                  │
  │                          │                       │ (exclui já agendados)  │                  │
  │◀─ {availableSlots} ──────│◀─ {availableSlots} ──│                       │                  │
  │                          │                       │                       │                  │
  │── POST /bookings ────────│── POST /bookings ────▶│                       │                  │
  │   {startDatetime: T}     │                       │── GET /schedule ─────▶│                  │
  │                          │                       │◀─ {price, duration} ──│                  │
  │                          │                       │                       │                  │
  │                          │                       │ Verifica slot livre    │                  │
  │                          │                       │ INSERT tb_bookings     │                  │
  │                          │                       │ (índice único garante) │                  │
  │                          │                       │                       │                  │
  │                          │                       │── booking.created ─────────────────────▶│
  │◀─ 201 {booking} ─────────│◀─ 201 {booking} ─────│                       │                  │
```

---

## 15. Ordem de Implementação Sugerida

1. **Setup do módulo** — `pom.xml`, `BookingApplication`, profiles yml, Dockerfile, `init.sql`
2. **Domínio** — `Booking`, `BookingStatus`, exceptions (TDD first)
3. **Ports** — `BookingRepository`, `CatalogServiceClient`, `BookingEventPublisher`
4. **Use Cases** — `CreateBookingUseCase`, `GetAvailableSlotsUseCase` (casos de maior valor)
5. **DB Migration + Adapter** — schema V1, `BookingEntity`, `PostgresBookingRepositoryAdapter`
6. **Catalog REST Client** — `CatalogServiceRestClient` adapter
7. **RabbitMQ Publisher** — `RabbitMQBookingEventPublisher`
8. **Controllers + DTOs** — `BookingController`, `AvailabilityController`
9. **Use Cases restantes** — Cancel, Complete, NoShow, List*
10. **BeanConfig** — wire use cases
11. **OpenAPI config** — Swagger com server URL do gateway
12. **Gateway update** — rota + Swagger URL
13. **Testes de integração** — Testcontainers (concorrência)
14. **BDD** — Cucumber features
15. **docker-compose** — adicionar booking-service
16. **k6** — script de carga
