# Search Service

Microsserviço de busca e descoberta do **Booking Hub**. Implementa o padrão **CQRS** — mantém um índice desnormalizado no **Elasticsearch** alimentado de forma assíncrona por eventos do RabbitMQ, e expõe uma API **GraphQL** para buscas ricas e filtros personalizáveis.

---

## Responsabilidades

- Consumir eventos do RabbitMQ para construir e manter o índice ES de estabelecimentos
- Expor busca por texto livre, cidade/estado, raio geográfico, serviço, rating e faixa de preço via GraphQL
- Calcular e manter `averageRating` de forma incremental (running average local — sem chamadas síncronas ao booking-service)
- Expor endpoint administrativo `POST /admin/reindex` para reconstrução do índice sob demanda

---

## Stack

| Camada | Tecnologia |
|--------|------------|
| Framework | Spring Boot 3.2 / Java 21 |
| API | **Spring for GraphQL** (`spring-boot-starter-graphql`) |
| Índice | Elasticsearch 8.13 |
| Spring Data ES | `spring-boot-starter-data-elasticsearch` |
| Mensageria | RabbitMQ (Spring AMQP) |
| HTTP client | Spring `RestClient` (para reindex) |
| Testes unitários | JUnit 5, Mockito |
| Testes BDD | Cucumber + REST Assured + Testcontainers (ElasticsearchContainer) |
| Cobertura | JaCoCo ≥ 80% de linhas |

---

## Arquitetura Interna (Clean / Hexagonal)

```
core/
  domain/         EstablishmentDocument, SearchFilter, SearchPage
  usecases/       IndexEstablishmentUseCase, IndexAffiliationUseCase,
                  IndexReviewUseCase, SearchEstablishmentsUseCase, ReindexUseCase
  ports/          EstablishmentSearchRepository, CatalogClient

application/
  dto/            SearchResultResponse, EstablishmentResultResponse

infrastructure/
  adapters/in/graphql/      SearchQueryResolver          ← @QueryMapping
  adapters/in/messaging/    EstablishmentEventListener,
                            AffiliationEventListener,
                            ReviewEventListener
  adapters/in/rest/         AdminReindexController       ← POST /admin/reindex
  adapters/out/elasticsearch/ EstablishmentEsDocument,
                              ElasticsearchSearchRepository,
                              ElasticsearchRepositoryAdapter
  adapters/out/catalog/     CatalogRestClient            ← usado só pelo ReindexUseCase
  configuration/            BeanConfig, RabbitMQConfig,
                            ElasticsearchConfig, GraphQLErrorConfig
```

---

## Endpoints

Porta local direta: `8085`  
Via API Gateway: `/api/search`

| Método | Path | Auth | Descrição |
|--------|------|------|-----------|
| `POST` | `/graphql` | Público | Endpoint GraphQL principal |
| `GET` | `/graphiql` | Público | Interface interativa (dev) |
| `POST` | `/admin/reindex` | `ROLE_OWNER` | Reconstrói índice ES a partir do catalog-service |

### Schema GraphQL

```graphql
type Query {
  searchEstablishments(filter: EstablishmentFilter!, page: PageInput): SearchResult!
}

input EstablishmentFilter {
  query:     String        # full-text: nome, descrição, serviços, profissionais
  city:      String        # filtro exato por cidade
  state:     String        # filtro exato por estado (ex: "SP")
  geo:       GeoFilter     # busca por raio geográfico
  services:  [String]      # estabelecimento deve oferecer todos os serviços listados
  minRating: Float         # avaliação média mínima (1.0–5.0)
  minPrice:  Float
  maxPrice:  Float
  sortBy:    SortBy        # RELEVANCE | RATING | DISTANCE
}

input GeoFilter { lat: Float!; lon: Float!; radiusKm: Float! }
input PageInput  { page: Int = 0; size: Int = 10 }
enum  SortBy     { RELEVANCE RATING DISTANCE }
```

### Exemplo de query GraphQL

```graphql
{
  searchEstablishments(
    filter: {
      geo: { lat: -23.56, lon: -46.63, radiusKm: 3.0 }
      minRating: 4.0
      sortBy: DISTANCE
    }
    page: { page: 0, size: 5 }
  ) {
    results {
      id name averageRating distanceKm
      services { title minPrice }
      professionals { name specialties }
    }
    totalHits
  }
}
```

---

## Eventos Consumidos

| Exchange | Routing Key | Fila | Origem | Ação |
|---|---|---|---|---|
| `catalog.events` | `establishment.created` | `search.establishment.created` | catalog-service | Upsert documento base |
| `catalog.events` | `establishment.updated` | `search.establishment.updated` | catalog-service | Upsert documento base |
| `catalog.events` | `affiliation.created` | `search.affiliation.created` | catalog-service | Upsert arrays `services[]` e `professionals[]` |
| `catalog.events` | `affiliation.updated` | `search.affiliation.updated` | catalog-service | Atualiza preços e profissional |
| `review.events` | `review.created` | `search.review.created` | booking-service | Incrementa `ratingSum`, recalcula `averageRating` |

Todos os listeners são **idempotentes**: reentregas do mesmo evento produzem o mesmo estado no ES (upsert por `id`).

---

## Atualização de Rating

O search-service **não faz chamadas síncronas em runtime**. Mantém `ratingSum` e `totalReviews` no documento ES e recalcula localmente ao consumir o evento `review.created` do booking-service:

```
doc.ratingSum     += establishmentRating
doc.totalReviews  += 1
doc.averageRating  = round(ratingSum / totalReviews, 1)
```

Implementado como **partial update** via `ElasticsearchOperations.update()`.

---

## Bootstrap — Reindex

Quando o search-service sobe pela primeira vez ou após perda do volume ES:

```bash
curl -X POST http://localhost:8080/api/search/admin/reindex \
  -H "Authorization: Bearer <OWNER_TOKEN>"
# → { "status": "accepted", "indexed": N }
```

O `ReindexUseCase` chama `GET /establishments` no catalog-service (única chamada REST síncrona, só sob demanda).

---

## Configuração

### Variáveis de ambiente (Docker)

| Variável | Descrição | Default (local) |
|---|---|---|
| `ELASTICSEARCH_HOST` | Host do ES | `localhost` |
| `ELASTICSEARCH_PORT` | Porta REST do ES | `9200` |
| `RABBIT_HOST` | Host do RabbitMQ | `localhost` |
| `RABBITMQ_PORT` | Porta AMQP | `5672` |
| `CATALOG_SERVICE_URI` | URL base do catalog-service | `http://localhost:8083` |

### Pré-requisito Linux/WSL2

Elasticsearch requer `vm.max_map_count=262144`:

```bash
sudo sysctl -w vm.max_map_count=262144
```

---

## Executar Localmente

```bash
# Requer Elasticsearch em localhost:9200 e RabbitMQ em localhost:5672
mvn spring-boot:run

# GraphiQL disponível em:
# http://localhost:8085/graphiql
```

---

## Testes

```bash
# Unitários (sem Spring context, sem Docker)
mvn test -Dtest="IndexEstablishmentUseCaseTest,IndexReviewUseCaseTest,IndexAffiliationUseCaseTest,ReindexUseCaseTest,SearchEstablishmentsUseCaseTest"

# BDD (requer Docker para Testcontainers)
mvn test -Dtest="CucumberTest"

# Todos + cobertura JaCoCo
mvn verify
```

Cobertura mínima: **80% de linhas**. Relatório em `target/site/jacoco/index.html`.

### Suíte de testes

| Classe | Tipo | Cenários |
|--------|------|----------|
| `IndexEstablishmentUseCaseTest` | Unit | Upsert de documento |
| `IndexReviewUseCaseTest` | Unit | Primeiro review (avg = rating), segundo review (média correta), rating null ignorado, estabelecimento não indexado |
| `IndexAffiliationUseCaseTest` | Unit | Adiciona profissional + serviços, estabelecimento não indexado, remove profissional inativo |
| `ReindexUseCaseTest` | Unit | Busca do catalog e upsert de todos, lista vazia |
| `SearchEstablishmentsUseCaseTest` | Unit | Retorna resultados, retorna vazio |
| `CucumberTest — search` | BDD | Busca por cidade, filtro por rating mínimo, busca por nome de profissional |
