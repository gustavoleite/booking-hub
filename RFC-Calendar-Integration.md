# RFC — Integração com Calendários Externos via Feed ICS

**Projeto:** Booking Hub
**Fase:** Tech Challenge Fase 3
**Requisito atendido:** Item 7 — Integração com calendários
**Status:** Aprovada
**Autor:** Equipe Booking Hub

---

## 1. Contexto e Motivação

O requisito 7 do Tech Challenge exige que profissionais e clientes possam sincronizar seus
agendamentos com calendários externos (Google Calendar, Microsoft Outlook, Apple Calendar etc.).

### Por que não usar OAuth2 direto com Google/Microsoft?

A abordagem OAuth2 direta exigiria:
- Registrar e ter aplicativos aprovados no Google Cloud Console e no Azure App Registrations
- Implementar e manter o fluxo Authorization Code (redirect, code exchange, CSRF)
- Armazenar e rotacionar `access_token` + `refresh_token` por usuário no banco
- Lidar com revogação, expiração e erros de consentimento

Para um projeto acadêmico, isso é desnecessariamente complexo e **não funciona sem aprovação
formal do provider** (apps OAuth em produção passam por verificação de semanas).

### Solução adotada: Feed ICS (iCalendar)

O protocolo iCalendar (RFC 5545) é suportado nativamente por **todos** os calendários modernos.
O usuário recebe uma URL de feed e a adiciona como "Assinar calendário" — o provider faz polling
periódico nessa URL e exibe os eventos. É exatamente o que Airbnb, Booking.com e Calendly usam.

```
Booking Hub → Feed ICS (URL estável por usuário)
                   ↑ polling a cada ~1h–12h
Google Calendar / Outlook / Apple Calendar
```

**Sem OAuth. Sem tokens externos. Funciona com qualquer calendário.**

---

## 2. Decisões de Design

### 2.1 Novo microsserviço: `notification-service`

A integração é implementada como um microsserviço independente (porta **8086**) para:

| Critério | Razão |
|---|---|
| Separação de responsabilidades | O `booking-service` é dono da lógica de negócio; geração de feeds é concern de infraestrutura |
| Falha isolada | Se o `notification-service` cair, os agendamentos continuam funcionando normalmente |
| Read model próprio | O serviço mantém um snapshot dos agendamentos via eventos RabbitMQ, sem acoplamento em tempo de leitura |

### 2.2 Fluxo de dados

```
booking-service
    │  publica BookingEventPayload
    ▼
RabbitMQ — exchange: booking.events (TopicExchange, durable)
    │  routing keys: booking.created / booking.cancelled / booking.completed
    ▼
notification-service (consumer — fila: calendar.sync.queue)
    │  upsert de BookingSnapshot no banco notification_db
    │
Google Calendar / Outlook / Apple Calendar
    │  GET /calendar/feed/{userId}/{feedToken}/bookings.ics  (polling)
    ▼
notification-service (REST)
    │  busca snapshots do usuário → gera ICS → retorna text/calendar
```

### 2.3 Autenticação do feed

A URL do feed **não usa JWT** (clientes de calendário não enviam headers de autenticação).
A segurança é garantida pelo `feedToken`: um UUID aleatório opaco gerado uma vez por usuário
e armazenado no banco. Sem o token correto, a URL não é válida.

```
webcal://booking-hub.com/api/calendar/feed/{userId}/{feedToken}/bookings.ics
```

Para obter a URL, o usuário chama `POST /calendar/feed/token` autenticado via JWT
(roteado pelo API Gateway com JwtAuthFilter).

---

## 3. Arquitetura (Clean Architecture)

```
notification-service/
├── core/
│   ├── domain/
│   │   ├── BookingSnapshot.java       — read model dos agendamentos
│   │   └── CalendarFeed.java          — userId + feedToken (URL segura)
│   ├── ports/
│   │   ├── BookingSnapshotRepository.java
│   │   └── CalendarFeedRepository.java
│   └── usecases/
│       ├── HandleBookingCreatedUseCase.java    — upsert snapshot (CONFIRMED)
│       ├── HandleBookingCancelledUseCase.java  — atualiza status CANCELLED
│       ├── HandleBookingCompletedUseCase.java  — atualiza status COMPLETED
│       ├── GenerateCalendarFeedUseCase.java    — valida token e gera ICS
│       └── GetOrCreateFeedTokenUseCase.java    — cria/retorna URL do feed
├── application/dto/
│   └── FeedUrlResponse.java
└── infrastructure/
    ├── adapters/
    │   ├── in/
    │   │   ├── rest/
    │   │   │   └── CalendarFeedController.java
    │   │   └── messaging/
    │   │       ├── BookingEventPayload.java
    │   │       └── RabbitMQBookingEventConsumer.java
    │   └── out/
    │       ├── database/
    │       │   ├── BookingSnapshotEntity.java
    │       │   ├── CalendarFeedEntity.java
    │       │   ├── JpaBookingSnapshotRepository.java
    │       │   ├── JpaCalendarFeedRepository.java
    │       │   ├── PostgresBookingSnapshotAdapter.java
    │       │   └── PostgresCalendarFeedAdapter.java
    │       └── ical/
    │           └── ICalendarGenerator.java     — gera texto .ics
    └── configuration/
        ├── BeanConfig.java
        ├── RabbitMQConsumerConfig.java
        └── OpenApiConfig.java
```

---

## 4. Modelo de Dados

### Tabela `booking_snapshots`

```sql
CREATE TABLE booking_snapshots (
    booking_id      UUID         PRIMARY KEY,
    client_id       VARCHAR(255) NOT NULL,
    professional_id UUID         NOT NULL,
    start_datetime  TIMESTAMPTZ  NOT NULL,
    end_datetime    TIMESTAMPTZ  NOT NULL,
    status          VARCHAR(20)  NOT NULL,   -- CONFIRMED | CANCELLED | COMPLETED | NO_SHOW
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_snapshots_client       ON booking_snapshots(client_id);
CREATE INDEX idx_snapshots_professional ON booking_snapshots(professional_id);
```

### Tabela `calendar_feeds`

```sql
CREATE TABLE calendar_feeds (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    VARCHAR(255) NOT NULL UNIQUE,
    feed_token VARCHAR(64)  NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

---

## 5. Contratos de API REST

### `POST /calendar/feed/token` — gerar URL do feed

```
Headers: X-User-Id: {userId}   (injetado pelo JwtAuthFilter do gateway)

Response 200:
{
  "feedUrl": "webcal://booking-hub.com/api/calendar/feed/{userId}/{feedToken}/bookings.ics"
}
```

O usuário copia essa URL e cola em:
- **Google Calendar** → Outros calendários → "De URL"
- **Outlook** → Adicionar calendário → "Assinar pela internet"
- **Apple Calendar** → Arquivo → Nova assinatura de calendário

### `GET /calendar/feed/{userId}/{feedToken}/bookings.ics` — feed ICS

```
Response 200:
Content-Type: text/calendar; charset=utf-8
Content-Disposition: inline; filename="bookings.ics"

BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//BookingHub//BookingHub Calendar//PT
CALNAME:BookingHub - Meus Agendamentos
BEGIN:VEVENT
UID:{bookingId}@bookinghub
DTSTAMP:{now}Z
DTSTART:{startDatetime}Z
DTEND:{endDatetime}Z
SUMMARY:Agendamento - BookingHub
STATUS:CONFIRMED
END:VEVENT
...
END:VCALENDAR
```

O `UID` sendo o `bookingId` garante **idempotência**: o provider atualiza o evento em vez de duplicar.
`STATUS:CANCELLED` faz o evento aparecer riscado/removido automaticamente.

---

## 6. Configuração RabbitMQ

```
Exchange: booking.events  (TopicExchange, durable — já existente no booking-service)

Nova fila: calendar.sync.queue  (durable, com dead-letter-exchange)
  binding: booking.created   → HandleBookingCreatedUseCase
  binding: booking.cancelled → HandleBookingCancelledUseCase
  binding: booking.completed → HandleBookingCompletedUseCase

Dead-letter:
  Exchange: calendar.dlx  (DirectExchange)
  Fila:     calendar.sync.dlq  (para reprocessamento manual)
```

Retry via Spring AMQP:
```yaml
spring:
  rabbitmq:
    listener:
      simple:
        retry:
          enabled: true
          initial-interval: 2000
          max-attempts: 5
          multiplier: 2.0
          max-interval: 30000
```

---

## 7. Integração com docker-compose e API Gateway

### `docker-compose.yml`

```yaml
notification-service:
  build:
    context: .
    dockerfile: notification-service/Dockerfile
  container_name: bw-notification-service
  ports:
    - "8086:8086"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - DB_HOST=postgres
    - DB_NAME=notification_db
    - DB_USER=admin
    - DB_PASS=admin123
    - RABBIT_HOST=rabbitmq
    - NOTIFICATION_BASE_URL=http://localhost:8080/api/calendar
  networks:
    - bw-network
  depends_on:
    postgres:
      condition: service_healthy
    rabbitmq:
      condition: service_healthy
```

### API Gateway — novas rotas

```yaml
# Feed público (sem JWT — o feedToken é a autenticação)
- id: notification-feed-route
  uri: http://notification-service:8086
  predicates:
    - Path=/api/calendar/feed/**
  filters:
    - RewritePath=/api/calendar/(?<remaining>.*), /${remaining}

# Geração de token (requer JWT)
- id: notification-token-route
  uri: http://notification-service:8086
  predicates:
    - Path=/api/calendar/**
  filters:
    - JwtAuthFilter
    - RewritePath=/api/calendar/(?<remaining>.*), /${remaining}
```

---

## 8. Estratégia de Testes

| Tipo | O que testar | Ferramenta |
|---|---|---|
| Unitários | Todos os 5 use cases, `ICalendarGenerator` | JUnit 5 + Mockito |
| BDD | "Dado que o usuário tem agendamentos, quando acessa o feed, então recebe ICS válido" | Cucumber |

---

## 9. Trade-offs e Limitações Conhecidas

| Trade-off | Decisão |
|---|---|
| **Sincronização unidirecional** | BookingHub → Calendário externo apenas. Eventos criados diretamente no Google/Outlook não voltam ao sistema. Escopo futuro: webhooks do Google/Microsoft. |
| **Atualização não é em tempo real** | Google Calendar faz polling a cada ~12h, Outlook a cada ~1h. Cancelamentos não aparecem instantaneamente. Aceitável para o contexto. |
| **Um feed por usuário** | O feed contém todos os agendamentos do usuário (como cliente e como profissional). Não há separação por calendário. Escopo futuro. |
| **Sem nomes de serviço/estabelecimento** | O `BookingEventPayload` carrega apenas IDs. O `SUMMARY` do evento fica genérico ("Agendamento - BookingHub"). Enriquecimento via chamada ao catalog-service é escopo futuro. |

---

## 10. Checklist de Implementação

- [x] `notification-service/pom.xml`
- [x] `notification-service/Dockerfile`
- [x] Domínio: `BookingSnapshot`, `CalendarFeed`
- [x] Ports: `BookingSnapshotRepository`, `CalendarFeedRepository`
- [x] Use cases: `HandleBookingCreated/Cancelled/Completed`, `GenerateCalendarFeed`, `GetOrCreateFeedToken`
- [x] Consumer RabbitMQ com DLQ
- [x] `ICalendarGenerator`
- [x] `CalendarFeedController`
- [x] Flyway migration `V1__create_notification_tables.sql`
- [x] `application.yml` / `application-docker.yml` / `application-test.yml`
- [x] Testes unitários (cobertura ≥ 80%)
- [x] Testes BDD (Cucumber)
- [x] Adicionado ao `docker-compose.yml`
- [x] Rotas no API Gateway
- [x] `notification_db` no `init.sql`
- [x] Módulo registrado no `pom.xml` raiz
