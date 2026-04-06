# RFC: Search Service

**Status:** Aprovado — todas as decisões fechadas  
**Data:** 2026-04-05  
**Autor:** Booking Hub Team

---

## 1. Contexto e Motivação

O sistema Booking Hub possui quatro serviços operacionais que acumulam dados de valor para o usuário final: `catalog-service` (estabelecimentos, profissionais, serviços e preços), `booking-service` (agendamentos e disponibilidade), `review-service` (avaliações e ratings) e `auth-service` (identidade).

O requisito 5 do Tech Challenge define que usuários devem poder **buscar estabelecimentos** por nome, localização, serviço oferecido ou avaliação, e aplicar **filtros avançados** por disponibilidade, faixa de preço e **critérios personalizáveis**.

Nenhum dos serviços existentes consegue atender esse requisito de forma isolada: o catálogo tem a estrutura, a review tem os ratings, o booking tem a disponibilidade real. Um motor de busca que consulte os três de forma síncrona seria lento e frágil.

A solução é um **`search-service` dedicado** com dois princípios norteadores:

- **CQRS:** mantém uma leitura desnormalizada e otimizada para busca no **Elasticsearch**, alimentada de forma assíncrona por eventos do RabbitMQ. O search-service nunca chama outro serviço em runtime de busca.
- **GraphQL:** adotado como protocolo de API para atender o requisito de "critérios personalizáveis" — filtros são expressos como um `input` tipado e extensível, e o cliente seleciona apenas os campos que precisa na resposta. Justificativa detalhada na seção 5.

---

## 2. Requisitos (origem: Tech-Challenge-Fase-3.md §5)

| # | Requisito |
|---|-----------|
| R1 | Buscar estabelecimentos por **nome** (texto livre, tolerante a erros parciais) |
| R2 | Buscar por **localização** — cidade/estado e raio em km (`geo_distance`) |
| R3 | Buscar por **serviço oferecido** (ex: "Corte Feminino", "Coloração") |
| R4 | Filtrar por **avaliação mínima** (ex: `minRating: 4`) |
| R5 | Filtrar por **faixa de preço** (`minPrice`, `maxPrice`) |
| R6 | Filtrar por **nome de profissional** (estabelecimentos onde o profissional atende) |
| R7 | Resultados ordenáveis por relevância, rating ou proximidade |
| R8 | Endpoints públicos (sem autenticação) |
| R9 | Alta cobertura de testes: TDD + BDD + JaCoCo ≥ 80% |

> **Nota R6 — disponibilidade:** A filtragem por disponibilidade em data/horário específico é responsabilidade do `booking-service` (`GET /api/bookings/availability`), que é a fonte de verdade para slots vagos em tempo real. O search-service retorna candidatos; a confirmação de vaga é feita separadamente. Essa separação elimina acoplamento síncrono no path de busca.

---

## 3. Visão Geral da Solução

```
┌────────────────────────────────────────────────────────────────────┐
│                       RabbitMQ (Event Bus)                         │
│                                                                    │
│  catalog.events ──► establishment.created   (NOVO no catalog)      │
│  catalog.events ──► establishment.updated   (NOVO no catalog)      │
│  catalog.events ──► affiliation.created     (já existe)            │
│  catalog.events ──► affiliation.updated     (NOVO no catalog)      │
│  review.events  ──► review.created          (já existe)            │
└──────────────────────────┬─────────────────────────────────────────┘
                           │  consome (assíncrono, sem acoplamento)
                           ▼
                ┌──────────────────────┐
                │    search-service    │  upsert
                │    (port 8085)       │ ─────────► Elasticsearch 8
                │                     │            índice: establishments
                │  GraphQL endpoint:  │
                │  POST /graphql      │◄─── clientes (Postman, frontend)
                └──────────────────────┘
```

**Invariantes de design:**
- O search-service **nunca chama outro serviço em runtime** (zero chamadas síncronas na path de busca)
- Toda escrita no índice acontece via eventos RabbitMQ
- O índice ES é um **read model** — nunca é fonte de verdade; divergências são transitórias
- Disponibilidade real é sempre consultada no `booking-service` diretamente pelo cliente

---

## 4. Protocolo: GraphQL

### 4.1 Justificativa

O requisito 5b especifica "filtros avançados e **outros critérios personalizáveis**". Em REST, cada novo critério adiciona um query param à URL e um `if` no use case. Com GraphQL, o `input EstablishmentFilter` é um tipo extensível — adicionar `openNow: Boolean` ou `minProfessionals: Int` não muda a URL nem quebra clientes existentes.

Benefícios adicionais neste contexto:
- **Seleção de campos:** cliente mobile recebe só `id + name + distanceKm`; cliente web recebe a resposta completa
- **Filtros compostos e tipados:** o schema garante em tempo de parse que `radiusKm` só faz sentido com `lat` e `lon`
- **GraphiQL:** interface interativa em `/graphiql` substitui Swagger para este serviço — o cliente explora filtros com autocompletar sem documentação extra
- **Extensibilidade sem versionamento de URL:** campo novo no `input` é opt-in para clientes

Trade-offs assumidos:
- O search-service **não aparece no Swagger UI centralizado** do gateway — ele tem seu próprio GraphiQL
- Testes BDD enviam POST com body GraphQL em vez de GET com query params — mais verbosos, mitigados por helpers no `StepDefinitions`
- Erros sempre retornam HTTP 200 com `errors[]` no body — tratamento via `DataFetcherExceptionResolver`
- Sem HTTP caching nativo (todos os requests são POST)

### 4.2 Schema GraphQL

```graphql
type Query {
  searchEstablishments(
    filter: EstablishmentFilter!
    page:   PageInput
  ): SearchResult!
}

# ── Inputs ────────────────────────────────────────────────────────

input EstablishmentFilter {
  query:      String        # full-text: nome, descrição, serviços, profissionais
  city:       String        # filtro exato por cidade
  state:      String        # filtro exato por estado (ex: "SP")
  geo:        GeoFilter     # busca por raio; requer lat/lon preenchidos no documento
  services:   [String]      # estabelecimento deve oferecer TODOS os serviços listados
  minRating:  Float         # avaliação média mínima (1.0 – 5.0)
  minPrice:   Float         # preço mínimo de qualquer serviço oferecido
  maxPrice:   Float         # preço máximo de qualquer serviço oferecido
  sortBy:     SortBy        # critério de ordenação (default: RELEVANCE)
}

input GeoFilter {
  lat:      Float!   # latitude do ponto de referência
  lon:      Float!   # longitude do ponto de referência
  radiusKm: Float!   # raio de busca em km
}

input PageInput {
  page: Int = 0
  size: Int = 10
}

enum SortBy {
  RELEVANCE   # _score ES × boost de rating (default)
  RATING      # averageRating DESC, depois _score
  DISTANCE    # geo_distance ASC — requer GeoFilter na query
}

# ── Tipos de resposta ─────────────────────────────────────────────

type SearchResult {
  results:   [EstablishmentResult!]!
  totalHits: Int!
  page:      Int!
  size:      Int!
}

type EstablishmentResult {
  id:            ID!
  name:          String!
  description:   String
  city:          String!
  state:         String!
  services:      [ServiceSummary!]!
  professionals: [ProfessionalSummary!]!
  minPrice:      Float
  maxPrice:      Float
  averageRating: Float       # null se sem reviews ainda
  totalReviews:  Int!
  distanceKm:    Float       # null se GeoFilter não foi fornecido
  score:         Float       # relevance score do ES
}

type ServiceSummary {
  title:    String!
  minPrice: Float
  maxPrice: Float
}

type ProfessionalSummary {
  name:       String!
  specialties: [String!]!
}
```

### 4.3 Exemplos de Queries

**Busca simples por texto e cidade:**
```graphql
{
  searchEstablishments(
    filter: { query: "corte feminino", city: "São Paulo" }
    page: { page: 0, size: 5 }
  ) {
    results { id name averageRating minPrice }
    totalHits
  }
}
```

**Busca por raio geográfico com filtro de preço:**
```graphql
{
  searchEstablishments(filter: {
    geo:      { lat: -23.56, lon: -46.63, radiusKm: 3.0 }
    maxPrice: 150.0
    minRating: 4.0
    sortBy:   DISTANCE
  }) {
    results { id name distanceKm averageRating services { title minPrice } }
    totalHits
  }
}
```

**Busca por profissional:**
```graphql
{
  searchEstablishments(filter: {
    query: "João Cabeleireiro"
    state: "SP"
  }) {
    results { id name professionals { name specialties } }
    totalHits
  }
}
```

**Campos mínimos (mobile):**
```graphql
{
  searchEstablishments(filter: { city: "Campinas", minRating: 4.5 }) {
    results { id name averageRating distanceKm }
    totalHits
  }
}
```

---

## 5. Domínio

### 5.1 Documento ES — `EstablishmentDocument`

```
EstablishmentDocument  (índice: establishments)
├── id:               keyword           ← UUID do estabelecimento
├── name:             text + keyword    ← full-text e exact
├── description:      text
├── city:             keyword
├── state:            keyword
├── zipCode:          keyword
├── geoPoint:         geo_point         ← OBRIGATÓRIO: lat/lon do endereço
├── services[]:       nested
│   ├── serviceId:   keyword
│   ├── title:       text + keyword
│   ├── minPrice:    float
│   └── maxPrice:    float
├── professionals[]:  nested
│   ├── professionalId: keyword
│   ├── name:           text
│   └── specialties[]:  text
├── minPrice:         float             ← menor preço entre todos os serviços
├── maxPrice:         float             ← maior preço entre todos os serviços
├── ratingSum:        float             ← soma acumulada dos ratings (não exposto na API)
├── averageRating:    float             ← ratingSum / totalReviews
├── totalReviews:     integer
└── updatedAt:        date
```

> **lat/lon obrigatórios:** o campo `geoPoint` é obrigatório no documento. O `catalog-service` passa a exigir `latitude` e `longitude` no DTO de criação de estabelecimento (`@NotNull`). Isso garante que geo-search funcione para 100% dos documentos indexados.

> **`ratingSum`:** campo interno, não exposto no schema GraphQL. Necessário para calcular `averageRating` de forma incremental a cada evento `review.created`, sem chamar o `review-service`.

> **Profissionais nested:** a busca por nome de profissional (`query: "João"`) faz match no campo `professionals[].name` via query nested do ES. O índice é único (`establishments`) — não há índice separado de profissionais.

### 5.2 Use Cases

| Use Case | Trigger | Ação no ES |
|----------|---------|------------|
| `IndexEstablishmentUseCase` | `establishment.created` ou `establishment.updated` | upsert do documento base (nome, endereço, geoPoint) |
| `IndexAffiliationUseCase` | `affiliation.created` ou `affiliation.updated` | upsert dos arrays `services[]` e `professionals[]` + recalcula `minPrice`/`maxPrice` |
| `IndexReviewUseCase` | `review.created` | incrementa `ratingSum` e `totalReviews`; recalcula `averageRating` |
| `SearchEstablishmentsUseCase` | query GraphQL | executa query ES com filtros, paginação e scoring |
| `ReindexUseCase` | `POST /admin/reindex` | chama `GET /catalog/establishments` e reconstrói o índice |

### 5.3 Ports

```java
// Porta de saída para o índice ES
interface EstablishmentSearchRepository {
    void upsert(EstablishmentDocument doc);
    void upsertPartial(String id, Map<String, Object> fields); // para rating update
    Optional<EstablishmentDocument> findById(String id);
    SearchPage<EstablishmentDocument> search(SearchFilter filter, Pageable pageable);
}

// Porta de saída para bootstrap (usada apenas pelo ReindexUseCase)
interface CatalogClient {
    List<EstablishmentSnapshot> fetchAllEstablishments();
}
```

---

## 6. Eventos Consumidos e Produzidos no Catalog-Service

### 6.1 Novos eventos a adicionar no `catalog-service`

O `catalog-service` atualmente publica apenas `affiliation.created`. Para alimentar o índice de forma event-driven pura, os seguintes eventos precisam ser adicionados:

| Routing Key | Quando publicar | Payload mínimo |
|---|---|---|
| `establishment.created` | `POST /catalog/establishments` com sucesso | `{ id, name, description, address: { city, state, zipCode, lat, lon } }` |
| `establishment.updated` | `PUT /catalog/establishments/{id}` com sucesso | mesmo payload acima |
| `affiliation.created` | já existe ✓ | `{ id, establishmentId, professionalId, professional: { name, specialties }, serviceOfferings: [{ title, price }] }` |
| `affiliation.updated` | quando preços, horários ou status da afiliação mudam | mesmo payload de `affiliation.created` |

**Impacto no `catalog-service`:**
- Adicionar `establishment.created` e `establishment.updated` ao `CatalogEventPublisher` port
- Publicar nos use cases `CreateEstablishmentUseCase` e `UpdateEstablishmentUseCase`
- Enriquecer o payload de `affiliation.created` com dados do profissional e dos serviços (atualmente publica o objeto `Affiliation` inteiro — verificar se já inclui os campos necessários)

### 6.2 Eventos consumidos pelo search-service

| Exchange | Routing Key | Origem | Use Case disparado |
|---|---|---|---|
| `catalog.events` | `establishment.created` | catalog-service | `IndexEstablishmentUseCase` |
| `catalog.events` | `establishment.updated` | catalog-service | `IndexEstablishmentUseCase` |
| `catalog.events` | `affiliation.created` | catalog-service | `IndexAffiliationUseCase` |
| `catalog.events` | `affiliation.updated` | catalog-service | `IndexAffiliationUseCase` |
| `review.events` | `review.created` | review-service | `IndexReviewUseCase` |

Todos os listeners são **idempotentes**: reentregas do mesmo evento produzem o mesmo estado no ES (upsert por `id`).

---

## 7. Atualização de Rating — Running Average Local

Ao receber `review.created`, o search-service **não chama o review-service**. Em vez disso, mantém `ratingSum` e `totalReviews` no documento ES e recalcula localmente:

```
// Ao receber review.created { establishmentId, establishmentRating }
doc.ratingSum      += establishmentRating   // null-safe: ignorar se null
doc.totalReviews   += 1
doc.averageRating   = round(ratingSum / totalReviews, 1)
```

Implementado como **partial update** via `ElasticsearchOperations.update()`:

```java
Document update = Document.create();
update.put("ratingSum",      existing.getRatingSum() + rating);
update.put("totalReviews",   existing.getTotalReviews() + 1);
update.put("averageRating",  round((existing.getRatingSum() + rating)
                               / (existing.getTotalReviews() + 1)));
operations.update(UpdateQuery.builder(id).withDocument(update).build(), index);
```

**Trade-off aceito:** se um evento `review.created` for perdido (DLQ, restart do consumer), o `averageRating` no ES pode divergir levemente do valor no `review-service`. Isso é aceitável para um read model de busca — o usuário que quer a nota exata consulta o detalhe do estabelecimento servido pelo `review-service`. O search-service usa a nota apenas como critério de ordenação e filtro aproximado.

---

## 8. Bootstrap e Recovery do Índice

### 8.1 Primeiro deploy

Quando o search-service sobe pela primeira vez, o índice ES está vazio. Os eventos anteriores já foram consumidos pelo RabbitMQ e não são reprocessados por padrão.

**Solução:** endpoint de reindex explícito, protegido, disparado manualmente após o deploy inicial:

```
POST /admin/reindex
Authorization: Bearer <OWNER_TOKEN>
→ 202 Accepted  (processamento assíncrono)
```

O `ReindexUseCase` executa:
1. `GET /api/catalog/establishments?page=0&size=1000` — carrega todos os estabelecimentos
2. Para cada estabelecimento, faz upsert no ES
3. Publica um log de progresso (opcional)

Essa é **a única chamada REST síncrona entre serviços** no search-service, e só acontece sob demanda explícita — não no startup.

### 8.2 Recovery após falha do ES

Se o volume do ES for perdido:
```bash
# 1. Subir o ES de volta
docker compose up -d elasticsearch

# 2. Aguardar healthcheck verde
# 3. Disparar reindex
curl -X POST http://localhost:8080/api/search/admin/reindex \
  -H "Authorization: Bearer <OWNER_TOKEN>"
```

### 8.3 Startup sem bloqueio

O search-service **não espera** o ES estar populado para subir. Buscas retornam array vazio enquanto o índice está sendo construído — comportamento esperado e documentado.

---

## 9. Estratégia de Ranking / Scoring

### `sortBy: RELEVANCE` (default)

ES `function_score` combina relevância textual (`BM25`) com boost proporcional ao rating:

```json
{
  "function_score": {
    "query": { "<<busca textual>>": {} },
    "functions": [{
      "field_value_factor": {
        "field":    "averageRating",
        "factor":   0.5,
        "modifier": "log1p",
        "missing":  1.0
      }
    }],
    "boost_mode": "multiply"
  }
}
```

Estabelecimentos com rating 5.0 aparecem antes de estabelecimentos igualmente relevantes com rating 3.0, mas a relevância textual não é ignorada.

### `sortBy: RATING`

```json
"sort": [{ "averageRating": "desc" }, { "_score": "desc" }]
```

### `sortBy: DISTANCE`

```json
"sort": [{
  "_geo_distance": {
    "geoPoint":    { "lat": <<lat>>, "lon": <<lon>> },
    "order":       "asc",
    "unit":        "km",
    "ignore_unmapped": false
  }
}]
```

Requer `geo` preenchido no `EstablishmentFilter`. Retorna `distanceKm` em cada resultado.

---

## 10. API — Endpoints Complementares (HTTP)

Além do endpoint GraphQL principal (`POST /graphql`), o search-service expõe:

| Método | Path | Auth | Descrição |
|--------|------|------|-----------|
| `POST` | `/graphql` | Público | Endpoint GraphQL principal |
| `GET` | `/graphiql` | Público | Interface interativa (dev/homologação) |
| `POST` | `/admin/reindex` | `ROLE_OWNER` | Reconstrói o índice ES a partir do catalog-service |
| `GET` | `/actuator/health` | Público | Health check |

O endpoint `/admin/reindex` é exposto via HTTP REST (não GraphQL) pois é uma operação administrativa pontual, não uma query de busca.

---

## 11. Arquitetura Interna (Clean / Hexagonal)

```
search-service/
├── core/
│   ├── domain/
│   │   ├── EstablishmentDocument.java   ← entidade de domínio (não anotada com ES)
│   │   └── SearchFilter.java            ← critérios de busca (record)
│   ├── usecases/
│   │   ├── IndexEstablishmentUseCase.java
│   │   ├── IndexAffiliationUseCase.java
│   │   ├── IndexReviewUseCase.java
│   │   ├── SearchEstablishmentsUseCase.java
│   │   └── ReindexUseCase.java
│   └── ports/
│       ├── EstablishmentSearchRepository.java
│       └── CatalogClient.javan
│
├── application/
│   └── dto/
│       ├── SearchResultResponse.java
│       └── EstablishmentResultResponse.java
│
└── infrastructure/
    ├── adapters/
    │   ├── in/
    │   │   ├── graphql/
    │   │   │   └── SearchQueryResolver.java      ← @QueryMapping
    │   │   ├── messaging/
    │   │   │   ├── EstablishmentEventListener.java
    │   │   │   ├── AffiliationEventListener.java
    │   │   │   └── ReviewEventListener.java
    │   │   └── rest/
    │   │       └── AdminReindexController.java   ← POST /admin/reindex
    │   └── out/
    │       ├── elasticsearch/
    │       │   ├── EstablishmentEsDocument.java  ← @Document ES
    │       │   ├── ElasticsearchSearchRepository.java
    │       │   └── ElasticsearchRepositoryAdapter.java
    │       └── catalog/
    │           └── CatalogRestClient.java        ← usado apenas pelo ReindexUseCase
    └── configuration/
        ├── BeanConfig.java
        ├── RabbitMQConfig.java
        ├── ElasticsearchConfig.java
        └── GraphQLErrorConfig.java               ← DataFetcherExceptionResolver
```

---

## 12. Stack e Dependências

| Camada | Tecnologia |
|--------|------------|
| Framework | Spring Boot 3.2 / Java 21 |
| API | **Spring for GraphQL** (`spring-boot-starter-graphql`) |
| Índice | Elasticsearch 8.13 |
| Spring Data ES | `spring-boot-starter-data-elasticsearch` |
| Mensageria | RabbitMQ (Spring AMQP) |
| HTTP client | Spring `RestClient` (para `CatalogRestClient`) |
| Testes unitários | JUnit 5, Mockito |
| Testes de integração | Testcontainers (`ElasticsearchContainer`) |
| Testes BDD | Cucumber + REST Assured (queries GraphQL via POST JSON) |
| Cobertura | JaCoCo ≥ 80% de linhas |

**Nota — sem Swagger:** o search-service não usa SpringDoc. A documentação interativa é o **GraphiQL** embutido no Spring for GraphQL, acessível em `http://localhost:8085/graphiql`.

---

## 13. Infraestrutura

### docker-compose additions

```yaml
elasticsearch:
  image: elasticsearch:8.13.0
  container_name: bw-elasticsearch
  environment:
    - discovery.type=single-node
    - xpack.security.enabled=false
    - ES_JAVA_OPTS=-Xms512m -Xmx512m
  ports:
    - "9200:9200"
  volumes:
    - esdata:/usr/share/elasticsearch/data
  networks:
    - bw-network
  healthcheck:
    test: ["CMD-SHELL", "curl -sf http://localhost:9200/_cluster/health?wait_for_status=yellow&timeout=5s"]
    interval: 10s
    timeout: 10s
    retries: 10

search-service:
  build:
    context: .
    dockerfile: search-service/Dockerfile
  container_name: bw-search-service
  ports:
    - "8085:8085"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - ELASTICSEARCH_HOST=elasticsearch
    - ELASTICSEARCH_PORT=9200
    - RABBIT_HOST=rabbitmq
    - CATALOG_SERVICE_URI=http://catalog-service:8083
    - OPENAPI_SERVER_URL=http://localhost:8080/api/search
  networks:
    - bw-network
  depends_on:
    elasticsearch:
      condition: service_healthy
    rabbitmq:
      condition: service_healthy
    catalog-service:
      condition: service_started
```

### Variáveis de ambiente

| Variável | Descrição | Default (local) |
|---|---|---|
| `ELASTICSEARCH_HOST` | Host do ES | `localhost` |
| `ELASTICSEARCH_PORT` | Porta REST do ES | `9200` |
| `RABBIT_HOST` | Host do RabbitMQ | `localhost` |
| `RABBITMQ_PORT` | Porta AMQP | `5672` |
| `CATALOG_SERVICE_URI` | URL base do catalog-service (reindex) | `http://localhost:8083` |

### Pré-requisito WSL2 / Docker Desktop (Windows)

Elasticsearch requer:
```bash
# No WSL2 ou host Linux
sudo sysctl -w vm.max_map_count=262144

# Para persistir entre reboots
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
```

---

## 14. Gateway — Roteamento

```yaml
# application-docker.yml e application-local.yml

# Rota para reindex (admin, protegida)
- id: search-service-admin-route
  uri: ${SEARCH_SERVICE_URI:http://search-service:8085}
  predicates:
    - Path=/api/search/admin/**
  filters:
    - JwtAuthFilter
    - RewritePath=/api/search/(?<remaining>.*), /${remaining}

# Rota principal GraphQL (pública)
- id: search-service-route
  uri: ${SEARCH_SERVICE_URI:http://search-service:8085}
  predicates:
    - Path=/api/search/**
  filters:
    - RewritePath=/api/search/(?<remaining>.*), /${remaining}
```

A rota de admin (`/api/search/admin/**`) aplica o `JwtAuthFilter`; a rota de busca (`/api/search/**`) é pública.

---

## 15. Estratégia de Testes

### 15.1 Unitários (TDD, sem Spring context)

| Classe | Cenários |
|--------|----------|
| `SearchEstablishmentsUseCase` | Filtro por cidade, por rating mínimo, por serviço, por geo, sem resultados, paginação |
| `IndexEstablishmentUseCase` | Criação de documento, atualização parcial, idempotência |
| `IndexReviewUseCase` | Primeiro review (averageRating = rating), segundo review (média correta), rating null ignorado |
| `IndexAffiliationUseCase` | Adiciona profissional, atualiza preços, recalcula minPrice/maxPrice |
| `ReindexUseCase` | Chama CatalogClient, itera estabelecimentos, faz upsert de cada um |

### 15.2 Integração (Testcontainers)

- `ElasticsearchSearchRepositoryTest` — `@SpringBootTest` com `@ServiceConnection ElasticsearchContainer`: testa upsert, partial update, geo query, nested query
- `SearchQueryResolverIntegrationTest` — Spring for GraphQL Test com `@GraphQlTest`: testa resolvers com repositório mockado

### 15.3 BDD / Cucumber

Feature files em `src/test/resources/features/`:

```gherkin
Feature: Buscar estabelecimentos

  Scenario: Busca por cidade retorna estabelecimentos indexados
    Given um estabelecimento "Salão da Maria" em "São Paulo" está indexado
    When uma query GraphQL busca estabelecimentos com city "São Paulo"
    Then a resposta contém ao menos 1 resultado
    And o resultado inclui o nome "Salão da Maria"

  Scenario: Filtro por rating mínimo exclui estabelecimentos abaixo do threshold
    Given dois estabelecimentos indexados com ratings 3.0 e 4.5
    When uma query GraphQL filtra por minRating 4.0
    Then apenas o estabelecimento com rating 4.5 aparece nos resultados

  Scenario: Busca por geo-distance retorna estabelecimentos no raio
    Given um estabelecimento indexado em lat -23.56 lon -46.63
    When uma query busca no raio de 1km a partir de lat -23.56 lon -46.63
    Then o estabelecimento aparece nos resultados com distanceKm menor que 1.0

  Scenario: Busca por nome de profissional retorna estabelecimento onde ele atua
    Given o profissional "João Cabeleireiro" está afiliado ao "Salão do João"
    When uma query busca establishments com query "João Cabeleireiro"
    Then o "Salão do João" aparece nos resultados

  Scenario: Reindex popula o índice a partir do catalog-service
    Given o índice ES está vazio
    And o catalog-service tem 3 estabelecimentos cadastrados
    When POST /admin/reindex é chamado com token ROLE_OWNER
    Then a busca retorna 3 resultados
```

---

## 16. Decisões Finais

| # | Decisão | Escolha | Motivo |
|---|---------|---------|--------|
| D1 | Protocolo de API | **GraphQL** | "Critérios personalizáveis" → filtros tipados e extensíveis; seleção de campos pelo cliente |
| D2 | Eventos faltantes | **Adicionar no catalog-service** | Event-driven puro; sem acoplamento síncrono em runtime |
| D3 | Bootstrap inicial | **`POST /admin/reindex` explícito** | Startup independente; reindex sob demanda e controlado |
| D4 | Atualização de rating | **Running average local** (`ratingSum + totalReviews`) | Zero chamadas ao review-service; resiliente a falhas |
| D5 | Disponibilidade | **Endpoints separados** | search retorna candidatos; `/api/bookings/availability` é a fonte de verdade |
| D6 | Lat/lon | **Obrigatórios** no catalog-service (`@NotNull`) | Geo-search funciona para 100% dos documentos; sem inconsistência de resultados |
| D7 | Índice de profissionais | **Nested em `establishments`** | Requisito orientado a estabelecimento; busca por profissional via nested query no mesmo índice |
| D8 | Consistência | **Eventual** (segundos) | Aceitável para busca; confirmação de booking sempre síncrona no booking-service |
| D9 | xpack.security | **Desabilitado** | Fase 3 acadêmica; sem overhead de configuração de certificados |
| D10 | Versão ES | **8.13** | Compatível com `spring-data-elasticsearch 5.2` (Spring Boot 3.2) |

---

## 17. Impactos em outros serviços

### catalog-service (modificações necessárias)

1. Tornar `latitude` e `longitude` **obrigatórios** no DTO `CreateEstablishmentRequest` (`@NotNull`)
2. Adicionar `establishment.created` ao `CatalogEventPublisher` — publicar em `CreateEstablishmentUseCase`
3. Adicionar `establishment.updated` — publicar em `UpdateEstablishmentUseCase`  
4. Adicionar `affiliation.updated` — publicar quando preços ou horários são alterados
5. Enriquecer payload de `affiliation.created/updated` com `professional.name`, `professional.specialties` e `serviceOfferings[].title`

### review-service (sem modificação)

O evento `review.created` já publica `establishmentId`, `establishmentRating` e `professionalRating` — suficiente para o `IndexReviewUseCase`.

### booking-service (sem modificação)

A disponibilidade é consultada diretamente pelo cliente via endpoint existente.

### api-gateway (adicionar rota)

Adicionar duas rotas: `search-service-admin-route` (com `JwtAuthFilter`) e `search-service-route` (pública). Ver seção 14.

---

## 18. Ordem de Implementação Sugerida

1. **Modificar `catalog-service`** — adicionar eventos + tornar lat/lon obrigatórios
2. **Setup do módulo** — `pom.xml`, `SearchApplication`, profiles yml, Dockerfile
3. **Infraestrutura ES** — `docker-compose.yml` + healthcheck + `vm.max_map_count`
4. **Domínio** — `EstablishmentDocument`, `SearchFilter`, exceptions (TDD)
5. **Ports** — `EstablishmentSearchRepository`, `CatalogClient`
6. **Use cases de indexação** — `IndexEstablishmentUseCase`, `IndexAffiliationUseCase`, `IndexReviewUseCase` (TDD)
7. **Adapter ES** — `ElasticsearchRepositoryAdapter` + mapeamento de índice
8. **Listeners RabbitMQ** — um listener por exchange/routing key
9. **Use case de busca** — `SearchEstablishmentsUseCase` com query builder ES
10. **Schema GraphQL** — `schema.graphqls`, `SearchQueryResolver`, `GraphQLErrorConfig`
11. **ReindexUseCase** + `CatalogRestClient` + `AdminReindexController`
12. **BeanConfig** — wire use cases e adapters
13. **Gateway** — adicionar rotas do search-service
14. **docker-compose** — adicionar `elasticsearch` e `search-service`
15. **Testes de integração** — Testcontainers ES
16. **BDD** — Cucumber features com helpers GraphQL
