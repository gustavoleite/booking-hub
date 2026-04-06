# Guia de Testes das APIs - Booking Hub

Este guia descreve o roteiro de testes para as APIs do projeto Booking Hub utilizando o Postman.

## 🚀 Pré-requisitos
- Ambiente rodando via Docker (`docker compose up -d`) ou localmente.
- Postman instalado.
- Importar a coleção disponível em: `docs/postman/Postman_Collection.postman_collection.json`.

## 📍 Configuração de Variáveis
As APIs utilizam as seguintes variáveis no Postman:
- `base_url`: `http://localhost:8080` (Endereço do API Gateway).
- `access_token`: Gerado automaticamente após o login.
- `x_user_id`: UUID do usuário (obtido no registro ou login).
- `establishment_id`: UUID do estabelecimento criado (salvo automaticamente pelo script do Postman).
- `professional_id`: UUID do profissional (salvo automaticamente pelo script do Postman).
- `service_id`: UUID do serviço criado no estabelecimento (salvo automaticamente pelo script do Postman).

---

## 🔐 1. Fluxo de Autenticação (Auth Service)

### 1.1 Registrar Usuário Owner
- **Endpoint:** `POST /api/auth/register`
- **Descrição:** Cria uma nova conta com role `ROLE_OWNER` para testes de estabelecimentos.
- **Roles disponíveis:** `ROLE_CLIENT`, `ROLE_PROFESSIONAL`, `ROLE_OWNER`.

### 1.2 Registrar Usuário Profissional
- **Endpoint:** `POST /api/auth/register`
- **Descrição:** Cria uma conta com role `ROLE_PROFESSIONAL` para testes de perfil e afiliações.

### 1.3 Login (Geração de Token)
- **Endpoint:** `POST /api/auth/login`
- **Descrição:** Autentica o usuário e salva o token JWT na variável `access_token`.
- **Nota:** O script de testes no Postman já está configurado para atualizar a variável automaticamente.

---

## 💇‍♂️ 2. Gestão de Profissionais (Catalog Service)

> Todos os endpoints de `/professionals/me` requerem `Authorization: Bearer {{access_token}}`.
> O API Gateway extrai `X-User-Id` do JWT automaticamente — não envie este header manualmente.

### 2.1 Criar Perfil Profissional
- **Endpoint:** `POST /api/catalog/professionals/me`
- **Role:** `ROLE_PROFESSIONAL`
- **Descrição:** Cria o perfil profissional do usuário autenticado.
- **Atenção:** Retorna `409 Conflict` se o perfil já existe. Use `PUT` para atualizar.

### 2.2 Obter Meu Perfil
- **Endpoint:** `GET /api/catalog/professionals/me`
- **Role:** `ROLE_PROFESSIONAL`
- **Descrição:** Recupera o perfil profissional do usuário autenticado.

### 2.3 Atualizar Perfil Profissional
- **Endpoint:** `PUT /api/catalog/professionals/me`
- **Role:** `ROLE_PROFESSIONAL`
- **Descrição:** Atualiza dados do perfil, como especialidades e bio.

### 2.4 Obter Perfil por ID (Público)
- **Endpoint:** `GET /api/catalog/professionals/{id}`
- **Autenticação:** Não requerida.
- **Descrição:** Recupera o perfil público de qualquer profissional pelo UUID.

---

## 🏪 3. Gestão de Estabelecimentos (Catalog Service - Requer ROLE_OWNER)

### 3.1 Criar Novo Estabelecimento
- **Endpoint:** `POST /api/catalog/establishments`
- **Descrição:** Cria um salão/barbearia.
- **Validações:**
  - O CNPJ deve ter 14 dígitos e passar no algoritmo de verificação (ex: `12345678000195`). Pontuação é removida automaticamente.
  - Pelo menos um serviço em `services` é obrigatório.
  - O campo `address` é obrigatório.
- **Corpo de exemplo:**
```json
{
  "name": "Barbearia Central",
  "cnpj": "12345678000195",
  "description": "O melhor corte da cidade.",
  "address": {
    "street": "Rua Principal",
    "number": "100",
    "city": "São Paulo",
    "state": "SP",
    "zipCode": "01234-567",
    "latitude": -23.5616,
    "longitude": -46.6565
  },
  "businessHours": [
    { "dayOfWeek": 1, "openTime": "08:00:00", "closeTime": "18:00:00" },
    { "dayOfWeek": 2, "openTime": "08:00:00", "closeTime": "18:00:00" }
  ],
  "services": [
    { "title": "Corte Simples", "description": "Corte de cabelo com máquina ou tesoura." }
  ]
}
```

### 3.2 Listar Meus Estabelecimentos
- **Endpoint:** `GET /api/catalog/establishments/my-salons`
- **Descrição:** Retorna todos os estabelecimentos ativos vinculados ao dono logado.

### 3.3 Obter Detalhes do Estabelecimento (Público)
- **Endpoint:** `GET /api/catalog/establishments/{id}`
- **Autenticação:** Não requerida.
- **Descrição:** Retorna detalhes completos do estabelecimento, incluindo serviços e horários.

### 3.4 Atualizar Estabelecimento
- **Endpoint:** `PUT /api/catalog/establishments/{id}`
- **Descrição:** Atualiza `name`, `description` e `photos` do estabelecimento. Os `businessHours` são substituídos pelos novos valores enviados.

### 3.5 Adicionar Serviço ao Estabelecimento
- **Endpoint:** `POST /api/catalog/establishments/{id}/services`
- **Descrição:** Adiciona um serviço ao catálogo do estabelecimento (operação aditiva — não remove serviços existentes referenciados por afiliações).

### 3.6 Inativar Estabelecimento
- **Endpoint:** `DELETE /api/catalog/establishments/{id}`
- **Descrição:** Marca o estabelecimento como inativo (soft delete). Retorna `204 No Content`.

---

## 🤝 4. Afiliações e Agenda (Catalog Service)

### 4.1 Vincular Profissional a Estabelecimento
- **Endpoint:** `POST /api/catalog/establishments/:establishmentId/affiliations?professionalId={{professional_id}}`
- **Role:** `ROLE_OWNER`
- **Descrição:** Vincula um profissional ao estabelecimento com serviços, preços e horários de trabalho.
- **Comportamento:** Se já existir uma afiliação para o mesmo par (estabelecimento + profissional), ela é **atualizada** (upsert).
- **Validação:** O `workSchedule` do profissional deve estar dentro dos `businessHours` do estabelecimento para o mesmo dia.
- **Corpo de exemplo:**
```json
{
  "active": true,
  "workSchedules": [
    { "dayOfWeek": 1, "startTime": "09:00:00", "endTime": "17:00:00" }
  ],
  "serviceOfferings": [
    {
      "providedServiceId": "{{service_id}}",
      "price": 75.0,
      "durationMinutes": 45
    }
  ]
}
```

### 4.2 Consultar Agenda do Profissional (Público)
- **Endpoint:** `GET /api/catalog/establishments/:establishmentId/affiliations/professional/:professionalId/schedule?serviceId={{service_id}}`
- **Autenticação:** Não requerida.
- **Descrição:** Retorna os horários de trabalho e o preço do serviço para o profissional no estabelecimento.
- **Dica:** O `serviceId` deve ser o UUID de um serviço já cadastrado e oferecido pelo profissional naquela afiliação.

---

---

## ⭐ 5. Avaliações (Review Service)

> Pré-requisito: o booking deve estar com status `COMPLETED` (Passo 9 do happy path). O review-service registra bookings elegíveis automaticamente ao consumir o evento `booking.completed`.

### 5.1 Submeter Avaliação
- **Endpoint:** `POST /api/reviews`
- **Role:** `ROLE_CLIENT`
- **Headers:** `Authorization: Bearer {{access_token}}`, `X-User-Id: {{x_user_id}}`
- **Corpo de exemplo:**
```json
{
  "bookingId": "{{booking_id}}",
  "professionalRating": 5,
  "establishmentRating": 4,
  "comment": "Atendimento excelente!"
}
```
- **Respostas de erro:**
  - `400` — Nenhum rating fornecido ou valor fora de 1–5
  - `409` — Booking já avaliado
  - `422` — Booking não concluído ou não encontrado
  - `403` — Booking pertence a outro cliente

### 5.2 Listar Reviews por Profissional (Público)
- **Endpoint:** `GET /api/reviews/professional/{professionalId}`
- **Autenticação:** Não requerida.

### 5.3 Stats de Rating do Profissional (Público)
- **Endpoint:** `GET /api/reviews/professional/{professionalId}/stats`
- **Retorna:** `{ "subjectId": "...", "averageRating": 4.7, "totalReviews": 42 }`

### 5.4 Listar Reviews por Estabelecimento (Público)
- **Endpoint:** `GET /api/reviews/establishment/{establishmentId}`

### 5.5 Stats de Rating do Estabelecimento (Público)
- **Endpoint:** `GET /api/reviews/establishment/{establishmentId}/stats`

---

## 🔍 6. Busca (Search Service — GraphQL)

O search-service expõe um endpoint GraphQL em `/api/search/graphql`. Todas as queries são **públicas** (sem autenticação). O GraphiQL interativo está disponível em http://localhost:8085/graphiql.

### 6.1 Busca por cidade
```bash
curl -X POST http://localhost:8080/api/search/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ searchEstablishments(filter: { city: \"São Paulo\" }) { results { id name averageRating } totalHits } }"}'
```

### 6.2 Busca por texto livre
```bash
curl -X POST http://localhost:8080/api/search/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ searchEstablishments(filter: { query: \"corte feminino\" }) { results { id name services { title minPrice } } totalHits } }"}'
```

### 6.3 Filtro por raio geográfico
```bash
curl -X POST http://localhost:8080/api/search/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ searchEstablishments(filter: { geo: { lat: -23.56, lon: -46.63, radiusKm: 3.0 }, sortBy: DISTANCE }) { results { id name distanceKm } totalHits } }"}'
```

### 6.4 Filtros combinados
```bash
curl -X POST http://localhost:8080/api/search/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ searchEstablishments(filter: { city: \"São Paulo\", minRating: 4.0, maxPrice: 200.0, sortBy: RATING }, page: { page: 0, size: 5 }) { results { id name averageRating minPrice maxPrice } totalHits } }"}'
```

### 6.5 Reindexar (ROLE_OWNER)
```bash
curl -X POST http://localhost:8080/api/search/admin/reindex \
  -H "Authorization: Bearer {{access_token}}"
# → { "status": "accepted", "indexed": N }
```

---

## 🌐 Gateway — Roteamento

O prefixo da URL indica para qual microsserviço a requisição será roteada:

| Prefixo | Serviço | Porta interna |
|---------|---------|---------------|
| `/api/auth/**` | auth-service | 8081 |
| `/api/bookings/**` | booking-service | 8082 |
| `/api/catalog/**` | catalog-service | 8083 |
| `/api/reviews/**` | review-service | 8084 |
| `/api/search/**` | search-service | 8085 |

O Gateway valida o JWT (RS256) e injeta os headers `X-User-Id` e `X-User-Role` automaticamente nos serviços downstream. Não é necessário enviar esses headers manualmente.

**Endpoints públicos** (sem autenticação): `/api/auth/**`, `GET /api/reviews/professional/**`, `GET /api/reviews/establishment/**`, `POST /api/search/graphql`, `GET /api/search/graphiql`.

---

## 🧪 Casos de Teste de Erro (Negativos)
1. **CNPJ Inválido:** Tente criar um estabelecimento com CNPJ `12345678000100` (dígitos verificadores errados). Esperado: `400 Bad Request`.
2. **Sem Serviços:** Tente criar um estabelecimento omitindo o campo `services`. Esperado: `400 Bad Request`.
3. **Perfil Profissional Duplicado:** Chame `POST /professionals/me` duas vezes com o mesmo usuário. Esperado: `409 Conflict`.
4. **E-mail Duplicado:** Registre um usuário com e-mail já existente. Esperado: `409 Conflict`.
5. **Horário Fora do Expediente:** Tente afiliar profissional com `startTime: "07:00:00"` quando o salão abre às `08:00:00`. Esperado: `400 Bad Request`.
6. **Serviço não Oferecido:** Consulte a agenda com um `serviceId` não cadastrado na afiliação. Esperado: `404 Not Found`.
7. **Sem Autenticação:** Acesse `GET /establishments/my-salons` sem Bearer token. Esperado: `401 Unauthorized`.
