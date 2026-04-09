# Notification Service

Microsserviço responsável pela **integração com calendários externos** via feed iCalendar (RFC 5545). Consome eventos de agendamento do RabbitMQ, mantém um read model dos agendamentos e expõe um endpoint público que retorna um arquivo `.ics` compatível com Google Calendar, Microsoft Outlook e Apple Calendar.

---

## Por que feed ICS e não OAuth2?

A abordagem direta com OAuth2 (Google Calendar API, Microsoft Graph) exige registro de aplicativo nos providers, fluxo Authorization Code com redirects, armazenamento de `access_token` + `refresh_token` por usuário e aprovação formal dos providers — inviável em contexto acadêmico.

O protocolo **iCalendar (RFC 5545)** é suportado nativamente por todos os calendários modernos. O usuário recebe uma URL estável e a adiciona como "assinar calendário" — o provider faz polling periódico nessa URL e exibe os eventos automaticamente. É exatamente o que Airbnb, Booking.com e Calendly usam.

```
Booking Hub ──→ Feed ICS (URL estável por usuário)
                      ↑ polling a cada ~1h–12h
Google Calendar / Outlook / Apple Calendar
```

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.2.3 |
| Web | Spring Web MVC |
| Mensageria | Spring AMQP (RabbitMQ) |
| Persistência | Spring Data JPA + PostgreSQL |
| Migração de schema | Flyway |
| Documentação | SpringDoc OpenAPI 3 (Swagger UI) |
| Testes unitários | JUnit 5 + Mockito |
| Testes BDD | Cucumber 7 + cucumber-spring |
| Testes (banco) | H2 in-memory |
| Build | Maven (multi-módulo, herda do parent) |
| Container | Docker (multi-stage build, eclipse-temurin:21-jre-alpine) |

---

## Arquitetura (Clean Architecture)

O serviço segue estritamente a Clean Architecture, com dependências apontando sempre para dentro — o domínio não conhece nenhum framework.

```
notification-service/src/main/java/com/bookinghub/notification/
│
├── core/                          ← Domínio e casos de uso (zero dependências externas)
│   ├── domain/
│   │   ├── BookingSnapshot.java   ← Read model dos agendamentos recebidos via eventos
│   │   └── CalendarFeed.java      ← Associação userId ↔ feedToken (URL segura)
│   ├── ports/
│   │   ├── BookingSnapshotRepository.java
│   │   └── CalendarFeedRepository.java
│   └── usecases/
│       ├── HandleBookingCreatedUseCase.java    ← Cria snapshot com status CONFIRMED
│       ├── HandleBookingCancelledUseCase.java  ← Atualiza snapshot para CANCELLED
│       ├── HandleBookingCompletedUseCase.java  ← Atualiza snapshot para COMPLETED
│       ├── GenerateCalendarFeedUseCase.java    ← Valida token e gera conteúdo ICS
│       └── GetOrCreateFeedTokenUseCase.java    ← Cria ou retorna URL do feed
│
├── application/
│   └── dto/
│       └── FeedUrlResponse.java
│
└── infrastructure/                ← Adaptadores (Spring, JPA, RabbitMQ)
    ├── adapters/
    │   ├── in/
    │   │   ├── rest/
    │   │   │   ├── CalendarFeedController.java
    │   │   │   └── GlobalExceptionHandler.java
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
    │           └── ICalendarGenerator.java
    └── configuration/
        ├── BeanConfig.java
        ├── RabbitMQConsumerConfig.java
        └── OpenApiConfig.java
```

### Regra de dependência

```
infrastructure → core ← (nada)
```

Os use cases recebem interfaces (`BookingSnapshotRepository`, `CalendarFeedRepository`) — nunca classes concretas do JPA ou RabbitMQ. A injeção de dependência é feita manualmente em `BeanConfig.java`, sem `@Autowired` nos use cases.

---

## Como funciona

### Fluxo de eventos (write side)

```
booking-service
    │  publica BookingEventPayload
    ▼
RabbitMQ — exchange: booking.events (TopicExchange, durable)
    │  routing key: booking.created   → HandleBookingCreatedUseCase   → status: CONFIRMED
    │  routing key: booking.cancelled → HandleBookingCancelledUseCase → status: CANCELLED
    │  routing key: booking.completed → HandleBookingCompletedUseCase → status: COMPLETED
    ▼
PostgreSQL — tabela: booking_snapshots (upsert por booking_id)
```

### Fluxo do feed ICS (read side)

```
Cliente / Profissional
    │  POST /calendar/feed/token  (requer JWT via API Gateway)
    ▼
GetOrCreateFeedTokenUseCase
    │  cria ou retorna CalendarFeed (userId + feedToken UUID opaco)
    ▼
Resposta: { "feedUrl": "webcal://.../{userId}/{feedToken}/bookings.ics" }

    │  usuário cola a URL no Google Calendar / Outlook / Apple Calendar
    ▼
Calendário externo faz polling periódico (sem JWT, apenas o token na URL)
    │  GET /calendar/feed/{userId}/{feedToken}/bookings.ics
    ▼
GenerateCalendarFeedUseCase
    │  valida feedToken → busca snapshots do userId (como cliente e como profissional)
    │  ICalendarGenerator converte snapshots em texto .ics
    ▼
Resposta: text/calendar (RFC 5545)
```

### Autenticação do feed

O endpoint de geração de token (`POST /calendar/feed/token`) exige JWT — o API Gateway injeta o `userId` via header `X-User-Id` após validar a assinatura RS256.

O endpoint do feed em si (`GET /calendar/feed/{userId}/{feedToken}/bookings.ics`) **não usa JWT** porque clientes de calendário (Google, Outlook, Apple) não enviam headers de autenticação. A segurança é o próprio `feedToken`: um UUID aleatório de 32 caracteres hexadecimais. Sem o token correto a URL retorna 400.

---

## API REST

Porta padrão: **8086**. Todas as rotas são acessíveis via API Gateway em `http://localhost:8080/api/calendar/...`.

### `POST /calendar/feed/token`

Gera ou retorna a URL do feed ICS do usuário autenticado. Idempotente — chamar duas vezes retorna a mesma URL.

**Headers (injetados pelo API Gateway após validação do JWT):**
```
X-User-Id: {userId}
```

**Response 200:**
```json
{
  "feedUrl": "webcal://localhost:8080/api/calendar/feed/{userId}/{feedToken}/bookings.ics"
}
```

---

### `GET /calendar/feed/{userId}/{feedToken}/bookings.ics`

Retorna o calendário ICS do usuário. Sem autenticação JWT — o `feedToken` é a credencial.

**Response 200:**
```
Content-Type: text/calendar; charset=utf-8
Content-Disposition: inline; filename="bookings.ics"

BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//BookingHub//BookingHub Calendar//PT
CALNAME:BookingHub - Meus Agendamentos
CALSCALE:GREGORIAN
METHOD:PUBLISH
BEGIN:VEVENT
UID:{bookingId}@bookinghub
DTSTAMP:{updatedAt}Z
DTSTART:{startDatetime}Z
DTEND:{endDatetime}Z
SUMMARY:Agendamento - BookingHub
STATUS:CONFIRMED
END:VEVENT
END:VCALENDAR
```

**Response 400:** token inválido para o `userId` informado.

O campo `UID` usa o `bookingId` como identificador único — o calendário atualiza o evento existente em vez de criar um duplicado quando o status muda. `STATUS:CANCELLED` faz o evento aparecer riscado ou removido automaticamente.

---

## Modelo de dados

```sql
-- Snapshot dos agendamentos (read model — alimentado via RabbitMQ)
CREATE TABLE booking_snapshots (
    booking_id      UUID         PRIMARY KEY,
    client_id       VARCHAR(255) NOT NULL,
    professional_id UUID         NOT NULL,
    start_datetime  TIMESTAMPTZ  NOT NULL,
    end_datetime    TIMESTAMPTZ  NOT NULL,
    status          VARCHAR(20)  NOT NULL,   -- CONFIRMED | CANCELLED | COMPLETED
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Feed token por usuário (associação userId ↔ URL segura)
CREATE TABLE calendar_feeds (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    VARCHAR(255) NOT NULL UNIQUE,
    feed_token VARCHAR(64)  NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

---

## RabbitMQ

| Exchange | Tipo | Durável |
|---|---|---|
| `booking.events` | TopicExchange | sim (pré-existente, criado pelo booking-service) |

| Fila | Routing keys consumidas | Dead-letter |
|---|---|---|
| `calendar.sync.queue` | `booking.created`, `booking.cancelled`, `booking.completed` | `calendar.dlx` → `calendar.sync.dlq` |

**Retry automático (Spring AMQP):**

| Parâmetro | Valor |
|---|---|
| `initial-interval` | 2 s |
| `max-attempts` | 5 |
| `multiplier` | 2.0 (backoff exponencial) |
| `max-interval` | 30 s |

Mensagens que esgotam as tentativas são enviadas para `calendar.sync.dlq` para reprocessamento manual.

---

## Como executar

### Pré-requisitos locais

- Java 21
- PostgreSQL rodando em `localhost:5432`
- RabbitMQ rodando em `localhost:5672`

Crie o banco antes do primeiro boot:

```sql
CREATE DATABASE notification_db;
```

### Rodar via Maven

```bash
# Na raiz do monorepo
mvn spring-boot:run -pl notification-service
```

Ou direto no módulo:

```bash
cd notification-service
mvn spring-boot:run
```

O Flyway aplica as migrations automaticamente na primeira inicialização.

### Rodar via Docker Compose (recomendado)

Sobe toda a stack (infra + todos os serviços):

```bash
# Na raiz do monorepo
docker compose up -d --build
```

O serviço ficará disponível em:
- **API:** `http://localhost:8086`
- **Swagger UI:** `http://localhost:8086/swagger-ui.html`
- **Via gateway:** `http://localhost:8080/api/calendar/...`

### Variáveis de ambiente (Docker)

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` | `postgres` | Host do PostgreSQL |
| `DB_NAME` | `notification_db` | Nome do banco |
| `DB_USER` | `admin` | Usuário do banco |
| `DB_PASS` | `admin123` | Senha do banco |
| `RABBIT_HOST` | `rabbitmq` | Host do RabbitMQ |
| `NOTIFICATION_BASE_URL` | `http://localhost:8080/api/calendar` | Base para construção da feedUrl |

---

## Testes

### Unitários (JUnit 5 + Mockito)

Testam cada use case e o gerador ICS isoladamente, sem Spring e sem banco. Rápidos — executam em memória pura.

```bash
mvn test -pl notification-service
```

Cobertura dos use cases:

| Classe testada | Cenários |
|---|---|
| `HandleBookingCreatedUseCase` | Salva snapshot com status CONFIRMED |
| `HandleBookingCancelledUseCase` | Atualiza para CANCELLED; ignora se não encontrado |
| `HandleBookingCompletedUseCase` | Atualiza para COMPLETED; ignora se não encontrado |
| `GetOrCreateFeedTokenUseCase` | Retorna URL existente; cria nova; URL começa com `webcal://` |
| `GenerateCalendarFeedUseCase` | Gera ICS válido; lança exceção para token inválido; inclui bookings do profissional quando userId é UUID |
| `ICalendarGenerator` | Estrutura VCALENDAR; VEVENT por snapshot; STATUS CANCELLED e CONFIRMED corretos |

### BDD (Cucumber + Spring Boot + H2)

Testes de comportamento escritos em linguagem natural. O contexto Spring sobe com H2 in-memory — RabbitMQ é mockado, nenhuma infraestrutura externa é necessária.

```bash
mvn test -pl notification-service -Dtest=CucumberRunner
```

Cenários cobertos (`src/test/resources/features/calendar_feed.feature`):

```gherkin
Scenario: User with bookings receives a valid ICS feed
Scenario: User with a cancelled booking sees it reflected in the feed
Scenario: Invalid token returns error
```

### Rodar apenas os testes unitários (sem BDD)

```bash
mvn test -pl notification-service -Dtest="HandleBookingCreatedUseCaseTest,HandleBookingCancelledUseCaseTest,HandleBookingCompletedUseCaseTest,GetOrCreateFeedTokenUseCaseTest,GenerateCalendarFeedUseCaseTest,ICalendarGeneratorTest"
```

---

## Testar manualmente com curl

> Os exemplos abaixo assumem que toda a stack está rodando via `docker compose up`.

**1. Registrar e autenticar um cliente:**

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"cliente@email.com","password":"Senha123!","role":"ROLE_CLIENT"}'

TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cliente@email.com","password":"Senha123!"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
```

**2. Criar um agendamento** (siga o happy path completo do README raiz — passos 1 a 7):

```bash
# Após criar o booking, o notification-service recebe automaticamente
# o evento booking.created via RabbitMQ e armazena o snapshot.
```

**3. Gerar a URL do feed ICS:**

```bash
curl -s -X POST http://localhost:8080/api/calendar/feed/token \
  -H "Authorization: Bearer $TOKEN"
# → { "feedUrl": "webcal://localhost:8080/api/calendar/feed/<userId>/<feedToken>/bookings.ics" }
```

**4. Baixar o feed ICS (sem JWT):**

```bash
# Substitua <userId> e <feedToken> pelos valores da resposta anterior
curl -s "http://localhost:8080/api/calendar/feed/<userId>/<feedToken>/bookings.ics"
```

**Saída esperada (com pelo menos um booking criado):**
```
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//BookingHub//BookingHub Calendar//PT
CALNAME:BookingHub - Meus Agendamentos
CALSCALE:GREGORIAN
METHOD:PUBLISH
BEGIN:VEVENT
UID:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx@bookinghub
DTSTAMP:20260409T120000Z
DTSTART:20260414T100000Z
DTEND:20260414T110000Z
SUMMARY:Agendamento - BookingHub
STATUS:CONFIRMED
END:VEVENT
END:VCALENDAR
```

**5. Adicionar ao Google Calendar:**

Copie a `feedUrl` (com `webcal://`) e acesse:
**Google Calendar → Outros calendários → + → De URL → cole a URL → Adicionar calendário**

O Google fará polling automático a cada ~12h. Para testar imediatamente, use a URL com `http://` no browser.

---

## Trade-offs conhecidos

| Decisão | Justificativa |
|---|---|
| Sincronização unidirecional (BookingHub → calendário externo) | Webhooks de retorno do Google/Microsoft requerem aprovação e endpoints públicos — fora do escopo |
| Atualização não é em tempo real | Google Calendar faz polling a cada ~12h, Outlook ~1h. Aceitável para confirmações de agendamento |
| Um feed por usuário (cliente + profissional combinados) | Simplifica a URL; separação por papel é escopo futuro |
| `SUMMARY` genérico ("Agendamento - BookingHub") | `BookingEventPayload` não inclui nome do serviço/estabelecimento; enriquecimento via chamada ao catalog-service é escopo futuro |
