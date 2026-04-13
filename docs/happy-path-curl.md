# Happy Path — Fluxo completo via cURL

Valida o caminho feliz de ponta a ponta: registro de usuários, criação de estabelecimento, agendamento, finalização, avaliação, busca e calendário.

Todo o tráfego passa pelo **API Gateway em `http://localhost:8080`**. Certifique-se de que a stack está no ar antes de começar — consulte [local-setup.md](local-setup.md).

---

## Atores

| Ator | Role | Responsabilidade no fluxo |
|---|---|---|
| **Owner** | `ROLE_OWNER` | Cria o estabelecimento, serviços e afilia o profissional |
| **Professional** | `ROLE_PROFESSIONAL` | Cria o perfil e finaliza o atendimento |
| **Client** | `ROLE_CLIENT` | Consulta disponibilidade, cria o booking e avalia |

---

## Passo 1 — Registrar os três usuários

```bash
# Owner
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"owner@salon.com","password":"Senha123!","role":"ROLE_OWNER"}'

# Professional
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"prof@salon.com","password":"Senha123!","role":"ROLE_PROFESSIONAL"}'

# Client
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"cliente@email.com","password":"Senha123!","role":"ROLE_CLIENT"}'
```

---

## Passo 2 — Autenticar e salvar os tokens

```bash
OWNER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"owner@salon.com","password":"Senha123!"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

PROF_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"prof@salon.com","password":"Senha123!"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

CLIENT_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cliente@email.com","password":"Senha123!"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "OWNER:  $OWNER_TOKEN"
echo "PROF:   $PROF_TOKEN"
echo "CLIENT: $CLIENT_TOKEN"
```

> Se preferir, faça os logins separadamente e exporte as variáveis manualmente:
> ```bash
> export OWNER_TOKEN="eyJ..."
> export PROF_TOKEN="eyJ..."
> export CLIENT_TOKEN="eyJ..."
> ```

---

## Passo 3 — Criar estabelecimento com serviços (Owner)

```bash
EST_RESP=$(curl -s -X POST http://localhost:8080/api/catalog/establishments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -d '{
    "name": "Salão da Maria",
    "cnpj": "12.345.678/0001-99",
    "description": "Salão completo de beleza",
    "address": {
      "street": "Rua das Flores",
      "number": "100",
      "city": "São Paulo",
      "state": "SP",
      "zipCode": "01310-100",
      "latitude": -23.5616,
      "longitude": -46.6565
    },
    "businessHours": [
      {"dayOfWeek": 1, "openTime": "09:00:00", "closeTime": "18:00:00"},
      {"dayOfWeek": 2, "openTime": "09:00:00", "closeTime": "18:00:00"},
      {"dayOfWeek": 3, "openTime": "09:00:00", "closeTime": "18:00:00"},
      {"dayOfWeek": 4, "openTime": "09:00:00", "closeTime": "18:00:00"},
      {"dayOfWeek": 5, "openTime": "09:00:00", "closeTime": "18:00:00"}
    ],
    "services": [
      {"title": "Corte Feminino", "description": "Corte e escova"},
      {"title": "Coloração", "description": "Coloração completa"}
    ]
  }')

echo $EST_RESP | python3 -m json.tool   # pretty print (opcional)

EST_ID=$(echo $EST_RESP | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
SVC_ID=$(echo $EST_RESP | grep -o '"providedServices":\[{"id":"[^"]*"' | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

echo "EST_ID: $EST_ID"
echo "SVC_ID: $SVC_ID"
```

> Anote manualmente `EST_ID` e `SVC_ID` caso o parsing automático não funcione no seu shell.

---

## Passo 4 — Criar perfil do profissional (Professional)

```bash
PROF_RESP=$(curl -s -X POST http://localhost:8080/api/catalog/professionals/me \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $PROF_TOKEN" \
  -d '{
    "name": "João Cabeleireiro",
    "bio": "10 anos de experiência em cortes femininos",
    "specialties": ["Corte", "Coloração"]
  }')

PROF_ID=$(echo $PROF_RESP | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

echo "PROF_ID: $PROF_ID"
```

---

## Passo 5 — Afiliar profissional ao estabelecimento com agenda (Owner)

```bash
curl -s -X POST \
  "http://localhost:8080/api/catalog/establishments/$EST_ID/affiliations?professionalId=$PROF_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -d '{
    "active": true,
    "workSchedules": [
      {"dayOfWeek": 1, "startTime": "09:00:00", "endTime": "18:00:00"},
      {"dayOfWeek": 2, "startTime": "09:00:00", "endTime": "18:00:00"},
      {"dayOfWeek": 3, "startTime": "09:00:00", "endTime": "18:00:00"},
      {"dayOfWeek": 4, "startTime": "09:00:00", "endTime": "18:00:00"},
      {"dayOfWeek": 5, "startTime": "09:00:00", "endTime": "18:00:00"}
    ],
    "serviceOfferings": [
      {"providedServiceId": "'"$SVC_ID"'", "price": 120.00, "durationMinutes": 60}
    ]
  }'
```

> Após este passo o search-service recebe os eventos `establishment.created` e `affiliation.created` e indexa o estabelecimento no Elasticsearch automaticamente.

---

## Passo 6 — Consultar disponibilidade (público, sem token)

Escolha uma data futura que caia em um dia útil (dayOfWeek 1–5). Exemplo com a próxima segunda-feira:

```bash
curl -s "http://localhost:8080/api/bookings/availability?\
establishmentId=$EST_ID&professionalId=$PROF_ID&serviceId=$SVC_ID&date=2026-04-13"
```

Resposta esperada:

```json
{
  "durationMinutes": 60,
  "price": 120.00,
  "availableSlots": [
    "2026-04-13T09:00:00",
    "2026-04-13T10:00:00",
    "2026-04-13T11:00:00",
    "..."
  ]
}
```

---

## Passo 7 — Criar agendamento (Client)

Use um dos horários retornados no passo anterior em `startDatetime`:

```bash
BOOKING_RESP=$(curl -s -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -d '{
    "professionalId": "'"$PROF_ID"'",
    "establishmentId": "'"$EST_ID"'",
    "providedServiceId": "'"$SVC_ID"'",
    "startDatetime": "2026-04-13T10:00:00",
    "notes": "Prefiro corte mais curto nas laterais"
  }')

BOOKING_ID=$(echo $BOOKING_RESP | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

echo "BOOKING_ID: $BOOKING_ID"
echo $BOOKING_RESP | python3 -m json.tool
```

Resposta esperada: `"status": "CONFIRMED"`.

> O notification-service consome o evento `booking.created` e envia e-mail de confirmação. Verifique em http://localhost:8025 (MailHog).

---

## Passo 8 — Consultar o agendamento (Client)

```bash
curl -s http://localhost:8080/api/bookings/$BOOKING_ID \
  -H "Authorization: Bearer $CLIENT_TOKEN" | python3 -m json.tool
```

---

## Passo 9 — Finalizar o atendimento (Professional)

```bash
curl -s -X PATCH http://localhost:8080/api/bookings/$BOOKING_ID/complete \
  -H "Authorization: Bearer $PROF_TOKEN" | python3 -m json.tool
```

Resposta esperada: `"status": "COMPLETED"`.

> O notification-service consome o evento `booking.completed` e envia e-mail de conclusão.

**Alternativa — cancelar (Client ou Owner):**

```bash
curl -s -X PATCH http://localhost:8080/api/bookings/$BOOKING_ID/cancel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -d '{"reason": "Compromisso surgiu"}' | python3 -m json.tool
```

---

## Passo 10 — Avaliar o atendimento (Client)

Só é possível após o status ser `COMPLETED`. O booking-service registra internamente o booking como elegível para avaliação ao completá-lo.

```bash
curl -s -X POST http://localhost:8080/api/reviews \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -d '{
    "bookingId": "'"$BOOKING_ID"'",
    "professionalRating": 5,
    "establishmentRating": 4,
    "comment": "Atendimento excelente!"
  }' | python3 -m json.tool
```

> O search-service consome o evento `review.created` e atualiza o `averageRating` do estabelecimento no Elasticsearch.

---

## Passo 11 — Buscar estabelecimentos via GraphQL (público)

Aguarde alguns segundos após os eventos do catalog e review serem processados.

**Busca por texto e cidade:**

```bash
curl -s -X POST http://localhost:8080/api/search/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ searchEstablishments(filter: { query: \"Salão\", city: \"São Paulo\" }) { totalHits results { id name averageRating totalReviews minPrice maxPrice services { title minPrice } } } }"
  }' | python3 -m json.tool
```

**Busca por raio geográfico ordenada por distância:**

```bash
curl -s -X POST http://localhost:8080/api/search/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ searchEstablishments(filter: { geo: { lat: -23.5616, lon: -46.6565, radiusKm: 5.0 }, sortBy: DISTANCE }) { totalHits results { id name distanceKm averageRating } } }"
  }' | python3 -m json.tool
```

**Busca combinada (texto + filtros + paginação):**

```bash
curl -s -X POST http://localhost:8080/api/search/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ searchEstablishments(filter: { query: \"Corte\", city: \"São Paulo\", minRating: 3.0, maxPrice: 200.0, sortBy: RELEVANCE }, page: { page: 0, size: 10 }) { totalHits page size results { id name city state averageRating totalReviews minPrice maxPrice score services { title minPrice maxPrice } professionals { name specialties } } } }"
  }' | python3 -m json.tool
```

> Use o **GraphiQL** em http://localhost:8085/graphiql para explorar o schema com autocompletar e documentação interativa.

---

## Passo 12 — Gerar feed de calendário ICS (Client)

```bash
# Obter a URL do feed (requer token)
FEED_RESP=$(curl -s -X POST http://localhost:8080/api/calendar/feed/token \
  -H "Authorization: Bearer $CLIENT_TOKEN")

FEED_URL=$(echo $FEED_RESP | grep -o '"feedUrl":"[^"]*"' | cut -d'"' -f4)

echo "Feed URL: $FEED_URL"
```

**Baixar o arquivo .ics:**

```bash
# Substituir webcal:// por http:// para uso via curl
HTTP_FEED_URL=$(echo $FEED_URL | sed 's/webcal:\/\//http:\/\//')

curl -s "$HTTP_FEED_URL"
```

Resposta esperada (RFC 5545):

```
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//BookingHub//BookingHub Calendar//PT
CALNAME:BookingHub - Meus Agendamentos
CALSCALE:GREGORIAN
METHOD:PUBLISH
BEGIN:VEVENT
UID:<bookingId>@bookinghub
DTSTAMP:20260413T100000Z
DTSTART:20260413T100000Z
DTEND:20260413T110000Z
SUMMARY:Agendamento - BookingHub
STATUS:CONFIRMED
END:VEVENT
END:VCALENDAR
```

**Integrar com calendário externo:**

Cole a `feedUrl` (com `webcal://`) em:
- **Google Calendar** → Outros calendários → "De URL"
- **Outlook** → Adicionar calendário → "Assinar pela internet"
- **Apple Calendar** → Arquivo → Nova assinatura de calendário

O feed é idempotente: cancelamentos alteram `STATUS:CANCELLED` no mesmo `UID` sem duplicar eventos.

---

## Passo 13 — Reindexar o Elasticsearch (se necessário)

Se o search-service subiu antes do catalog ou o volume do Elasticsearch foi perdido:

```bash
curl -s -X POST http://localhost:8080/api/search/admin/reindex \
  -H "Authorization: Bearer $OWNER_TOKEN" | python3 -m json.tool
# → { "status": "accepted", "indexed": 1 }
```

---

## Resumo do fluxo de eventos

```
[Owner] registra → [Owner] cria estabelecimento + serviços
  → catalog publica: establishment.created
  → search-service indexa o estabelecimento

[Owner] afilia profissional com agenda e preços
  → catalog publica: affiliation.created
  → search-service adiciona profissionais e serviços ao índice

[Client] consulta disponibilidade (sem token)
[Client] cria booking → status: CONFIRMED
  → booking publica: booking.created
  → notification-service salva snapshot + envia e-mail de confirmação

[Professional] finaliza → status: COMPLETED
  → booking publica: booking.completed
  → booking registra booking elegível (internamente)
  → notification-service atualiza snapshot + envia e-mail de conclusão

[Client] avalia o atendimento
  → booking publica: review.created
  → search-service atualiza averageRating no índice

[Client] busca via GraphQL → resultados com rating e distância atualizados
[Client] gera feed ICS → integra com Google Calendar / Outlook / Apple Calendar
```
