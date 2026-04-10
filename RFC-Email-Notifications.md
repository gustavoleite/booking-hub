# RFC — Confirmações e Lembretes Automáticos por E-mail

**Projeto:** Booking Hub
**Fase:** Tech Challenge Fase 3
**Requisito atendido:** Item 3b — Confirmações e lembretes automáticos para cliente e profissional
**Status:** Proposta
**Autor:** Equipe Booking Hub

---

## 1. Contexto e Motivação

O requisito 3b exige que o sistema envie confirmações e lembretes automáticos tanto para o cliente quanto para o profissional em resposta a eventos de agendamento.

O `notification-service` já existe e já consome eventos do RabbitMQ para alimentar o feed ICS (requisito 7a). A infraestrutura de mensageria está funcionando. O que falta é:

1. **Confirmações** — e-mail disparado imediatamente quando um booking é criado, cancelado ou finalizado.
2. **Lembretes** — e-mail disparado automaticamente ~24h antes do agendamento.

---

## 2. Problema: os eventos não carregam e-mails

O `BookingEventPayload` publicado pelo `booking-service` contém apenas IDs:

```java
public record BookingEventPayload(
    UUID bookingId,
    String clientId,       // UUID do usuário (subject do JWT)
    UUID professionalId,   // UUID do usuário profissional
    ...
)
```

O e-mail dos usuários vive no `auth-service`. O `notification-service` precisa dessas informações para enviar e-mails.

### Por que não chamar o auth-service em tempo de processamento?

O `notification-service` é um consumidor assíncrono. Criar uma dependência síncrona em tempo de consumo de evento viola o isolamento de falhas — se o `auth-service` estiver lento ou indisponível, a fila de eventos se acumula e as confirmações param.

### Solução adotada: enriquecer o evento na origem

O `booking-service` já tem o e-mail do cliente disponível no header `X-User-Email` (injetado pelo gateway). Para o profissional, ele faz uma chamada síncrona ao `auth-service` no momento da criação do booking — a mesma janela em que já valida disponibilidade no `catalog-service`. Falha de um é falha do outro: o booking não é criado sem as informações.

```
booking-service (ao criar booking)
    │  já tem: clientEmail (header X-User-Email)
    │  busca:  professionalEmail → GET /internal/users/{professionalId}/email
    ▼
BookingEventPayload (enriquecido)
    + clientEmail
    + professionalEmail
    + establishmentName  (já disponível via catalog-service, mesma chamada existente)
    + serviceName
```

Essa abordagem mantém o `notification-service` completamente autônomo — ele não precisa chamar ninguém para enviar o e-mail.

---

## 3. Mudanças por serviço

### 3.1 API Gateway — injetar `X-User-Email`

O JWT emitido pelo `auth-service` usa o UUID como `subject`, mas já inclui o e-mail como claim customizado (pode ser adicionado). O gateway injeta o e-mail via header, da mesma forma que já injeta `X-User-Id`.

**Mudança em `NimbusTokenGeneratorAdapter.java` (auth-service):**
```java
// Adicionar claim de e-mail ao JWT
.claim("email", user.getEmail())
.subject(user.getId().toString())
```

**Mudança em `JwtAuthFilter.java` (api-gateway):**
```java
.header("X-User-Id", claims.getSubject())
.header("X-User-Email", claims.getStringClaim("email"))  // novo
```

---

### 3.2 Auth Service — endpoint interno de lookup

Para o `booking-service` buscar o e-mail do profissional (que não está no header da requisição do cliente), o `auth-service` expõe um endpoint interno:

```
GET /internal/users/{userId}/email
→ 200: { "email": "prof@salon.com" }
→ 404: usuário não encontrado
```

Este endpoint **não passa pelo `JwtAuthFilter`** do gateway — é rota interna entre serviços, acessada diretamente via nome do container Docker (`auth-service:8081`). A proteção é de rede (apenas containers na mesma `bw-network` podem acessar).

**Novos arquivos no auth-service:**
- `InternalUserController.java` — endpoint `/internal/users/{id}/email`
- `GetUserEmailUseCase.java`

---

### 3.3 Booking Service — enriquecer o evento

**Mudanças:**

`BookingEventPayload.java` — novos campos:
```java
public record BookingEventPayload(
    UUID bookingId,
    String clientId,
    String clientEmail,       // novo
    UUID professionalId,
    String professionalEmail, // novo
    String establishmentName, // novo
    String serviceName,       // novo
    ...
)
```

`AuthServiceClient.java` (nova porta):
```java
public interface AuthServiceClient {
    String getEmailByUserId(String userId);
}
```

`RestAuthServiceClient.java` (novo adaptador):
```java
// Chama GET http://auth-service:8081/internal/users/{userId}/email
```

`CreateBookingUseCase.java` — usa `AuthServiceClient` para buscar e-mail do profissional e recebe `clientEmail` como parâmetro (vindo do controller via `X-User-Email`).

`BookingController.java` — passa `X-User-Email` do header ao use case.

---

### 3.4 Notification Service — envio de e-mails e lembretes

#### 3.4.1 Nova dependência

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

#### 3.4.2 Novos campos no `BookingSnapshot`

```sql
ALTER TABLE booking_snapshots ADD COLUMN client_email    VARCHAR(255);
ALTER TABLE booking_snapshots ADD COLUMN professional_email VARCHAR(255);
ALTER TABLE booking_snapshots ADD COLUMN establishment_name VARCHAR(255);
ALTER TABLE booking_snapshots ADD COLUMN service_name    VARCHAR(255);
ALTER TABLE booking_snapshots ADD COLUMN reminder_sent   BOOLEAN NOT NULL DEFAULT FALSE;
```

Migration: `V2__add_notification_fields_to_snapshots.sql`

#### 3.4.3 Novos use cases

| Use Case | Trigger | Destinatários |
|---|---|---|
| `SendBookingConfirmationUseCase` | `booking.created` | Cliente + Profissional |
| `SendBookingCancellationUseCase` | `booking.cancelled` | Cliente + Profissional |
| `SendBookingCompletedUseCase` | `booking.completed` | Cliente (link para avaliação) |
| `SendBookingReminderUseCase` | Scheduler (24h antes) | Cliente + Profissional |

#### 3.4.4 Conteúdo dos e-mails

**Confirmação (booking.created):**
```
Assunto: Agendamento confirmado — {serviceName} em {establishmentName}
Para:    {clientEmail}, {professionalEmail}

Olá!

Seu agendamento foi confirmado:
  Serviço:      {serviceName}
  Estabelecimento: {establishmentName}
  Data e hora:  {startDatetime}
  Duração:      {durationMinutes} min

Adicione ao seu calendário:
  {feedUrl}

Booking Hub
```

**Lembrete (24h antes):**
```
Assunto: Lembrete — seu agendamento é amanhã!
Para:    {clientEmail}, {professionalEmail}

Não esqueça! Você tem um agendamento amanhã:
  Serviço:      {serviceName}
  Data e hora:  {startDatetime}
```

**Cancelamento (booking.cancelled):**
```
Assunto: Agendamento cancelado — {serviceName}
Para:    {clientEmail}, {professionalEmail}

Seu agendamento foi cancelado.
  Serviço:      {serviceName}
  Data e hora:  {startDatetime}
```

#### 3.4.5 Scheduler de lembretes

```java
@Scheduled(fixedDelay = 3_600_000) // a cada 1h
public void sendReminders() {
    LocalDateTime windowStart = LocalDateTime.now().plusHours(23);
    LocalDateTime windowEnd   = LocalDateTime.now().plusHours(25);
    // busca snapshots com status CONFIRMED, reminderSent=false,
    // startDatetime entre windowStart e windowEnd
    // envia e-mail e marca reminderSent=true
}
```

A janela de 23h–25h garante que o lembrete seja enviado exatamente uma vez, mesmo que o scheduler rode em qualquer minuto da hora.

`@EnableScheduling` é adicionado em `NotificationApplication.java`.

#### 3.4.6 Novo adaptador de e-mail

```
infrastructure/adapters/out/email/
    EmailSenderAdapter.java     ← implementa EmailPort usando JavaMailSender
    EmailPort.java              ← porta (interface no core)
```

```java
public interface EmailPort {
    void send(String to, String subject, String body);
}
```

---

## 4. Infraestrutura local — MailHog

Para desenvolvimento e testes locais, os e-mails são capturados pelo **MailHog** — um servidor SMTP fake que nunca entrega para destinatários reais e expõe uma UI web para visualização.

**Adição ao `docker-compose.yml`:**
```yaml
mailhog:
  image: mailhog/mailhog:latest
  container_name: bw-mailhog
  ports:
    - "1025:1025"   # SMTP — notification-service aponta aqui
    - "8025:8025"   # UI web — http://localhost:8025
  networks:
    - bw-network
```

**`application-docker.yml` do notification-service:**
```yaml
spring:
  mail:
    host: ${MAIL_HOST:mailhog}
    port: ${MAIL_PORT:1025}
    username: ""
    password: ""
    properties:
      mail.smtp.auth: false
      mail.smtp.starttls.enable: false
```

**`application.yml` (local):**
```yaml
spring:
  mail:
    host: localhost
    port: 1025
```

Para produção, substituir MailHog por SendGrid, AWS SES ou SMTP real via variáveis de ambiente.

---

## 5. Fluxo completo

```
[booking.created]
booking-service
    │  clientEmail (X-User-Email header)
    │  professionalEmail (GET auth-service:8081/internal/users/{id}/email)
    │  establishmentName, serviceName (já disponível via catalog-service)
    ▼
RabbitMQ → booking.events (BookingEventPayload enriquecido)
    ▼
notification-service (consumer)
    │  HandleBookingCreatedUseCase:
    │    1. upsert BookingSnapshot (com emails)
    │    2. SendBookingConfirmationUseCase → e-mail para cliente + profissional
    ▼
MailHog / SMTP real

[scheduler — a cada 1h]
notification-service
    │  busca snapshots CONFIRMED, reminderSent=false, startDatetime ∈ [now+23h, now+25h]
    │  SendBookingReminderUseCase → e-mail para cliente + profissional
    │  marca reminderSent=true
```

---

## 6. Modelo de dados atualizado

```
booking_snapshots (V2 migration — novos campos):
  + client_email       VARCHAR(255)
  + professional_email VARCHAR(255)
  + establishment_name VARCHAR(255)
  + service_name       VARCHAR(255)
  + reminder_sent      BOOLEAN DEFAULT FALSE
```

Nenhuma nova tabela é necessária — os dados de contato viajam no próprio snapshot.

---

## 7. Estratégia de testes

| Tipo | O que testar |
|---|---|
| Unitário | `SendBookingConfirmationUseCase`, `SendBookingCancellationUseCase`, `SendBookingCompletedUseCase`, `SendBookingReminderUseCase` — `EmailPort` mockado |
| Unitário | `ScheduledReminderJob` — verifica que o use case é invocado para os snapshots elegíveis |
| BDD | "Dado que um booking foi criado com e-mails, quando o evento é processado, então dois e-mails são enviados" |
| Integração (manual) | Subir stack com MailHog, criar booking via Postman, verificar e-mail em http://localhost:8025 |

---

## 8. Checklist de implementação

**Auth Service:**
- [ ] Adicionar claim `email` ao JWT em `NimbusTokenGeneratorAdapter`
- [ ] `InternalUserController` — `GET /internal/users/{id}/email`
- [ ] `GetUserEmailUseCase`

**API Gateway:**
- [ ] Injetar `X-User-Email` no `JwtAuthFilter`

**Booking Service:**
- [ ] `AuthServiceClient` (porta + adaptador REST)
- [ ] Enriquecer `BookingEventPayload` com `clientEmail`, `professionalEmail`, `establishmentName`, `serviceName`
- [ ] `CreateBookingUseCase` — recebe e passa os e-mails
- [ ] `BookingController` — extrai `X-User-Email` do header

**Notification Service:**
- [ ] `spring-boot-starter-mail` no `pom.xml`
- [ ] `EmailPort` (interface no core)
- [ ] `EmailSenderAdapter` (infraestrutura)
- [ ] `V2__add_notification_fields_to_snapshots.sql`
- [ ] `BookingSnapshot` — novos campos
- [ ] `SendBookingConfirmationUseCase`
- [ ] `SendBookingCancellationUseCase`
- [ ] `SendBookingCompletedUseCase`
- [ ] `SendBookingReminderUseCase`
- [ ] `ScheduledReminderJob` + `@EnableScheduling`
- [ ] Atualizar `HandleBookingCreated/Cancelled/Completed` para acionar envio
- [ ] `application.yml` / `application-docker.yml` — config de SMTP
- [ ] Testes unitários para todos os use cases novos
- [ ] Testes BDD — cenário de envio de confirmação

**Infra:**
- [ ] MailHog no `docker-compose.yml`
- [ ] `MAIL_HOST` / `MAIL_PORT` no docker-compose `notification-service`

---

## 9. Trade-offs e limitações

| Trade-off | Decisão |
|---|---|
| Enriquecer evento vs. lookup em runtime | Enriquecer na origem — mantém notification-service autônomo e resiliente |
| Template em string vs. Thymeleaf | String simples — sem dependência extra; suficiente para o escopo acadêmico |
| MailHog vs. SMTP real | MailHog em dev/docker; SMTP real (SendGrid/SES) via env var em produção |
| Scheduler vs. mensagem com TTL | `@Scheduled` — mais simples; mensagem com TTL seria mais precisa mas exige infraestrutura extra de RabbitMQ |
| Um lembrete por booking | `reminderSent` flag garante idempotência — sem duplicatas mesmo se o scheduler reiniciar |
