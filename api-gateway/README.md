# API Gateway

Ponto único de entrada do Booking Hub. Implementado com **Spring Cloud Gateway** (WebFlux/reativo), concentra três responsabilidades: roteamento dinâmico para os microsserviços internos, validação de JWT e política de CORS — sem estado de sessão e sem banco de dados.

---

## Stack e justificativa

| Tecnologia | Justificativa |
|---|---|
| **Spring Cloud Gateway** | Solução nativa do ecossistema Spring para API Gateway reativo. Construída sobre WebFlux/Netty — processa requests sem bloquear threads, essencial para um edge service com alto volume de conexões simultâneas |
| **JJWT 0.11** | Biblioteca leve para parsing e validação de JWT. O gateway só valida a assinatura com a chave pública RSA — não precisa do SDK completo do auth-service |
| **SpringDoc OpenAPI (WebFlux)** | Agrega as especificações `/v3/api-docs` de todos os serviços em uma única Swagger UI centralizada |
| **Spring WebFlux** | Base do Spring Cloud Gateway. Modelo reativo (Project Reactor) garante baixa latência e boa utilização de recursos em I/O-bound workloads como proxy HTTP |

---

## Arquitetura interna

```
core/
  domain/exceptions/   InvalidAuthorizationHeaderException, InvalidTokenException,
                       JwtConfigurationException, MissingTokenException, UnauthorizedException

  application/services/
    JwtValidationService   ← carrega RSA public key (PEM), valida assinatura e expiry via JJWT,
                             retorna Claims

infrastructure/
  config/
    GlobalFilterConfig     ← registra a Swagger UI agregada (/swagger-ui.html)
  web/filters/
    JwtAuthFilter          ← GatewayFilterFactory — extrai e valida Bearer token,
                             injeta headers X-User-Id, X-User-Role, X-User-Email
```

---

## Rotas

Todas as rotas reescrevem o prefixo `/api/<serviço>` antes de encaminhar para o serviço destino.

| Rota | Predicate | JWT obrigatório | Destino |
|---|---|---|---|
| `/api/auth/**` | `Path` | Não | auth-service:8081 |
| `/api/catalog/**` | `Path` | Sim | catalog-service:8083 |
| `/api/bookings/**` | `Path` | Sim | booking-service:8082 |
| `/api/reviews/**` | `Path` | Sim | booking-service:8082 |
| `/api/search/admin/**` | `Path` | Sim | search-service:8085 |
| `/api/search/**` | `Path` | Não | search-service:8085 |
| `GET /api/calendar/feed/**` | `Path + Method=GET` | Não (feedToken na URL) | notification-service:8086 |
| `POST /api/calendar/feed/token` | `Path + Method=POST` | Sim | notification-service:8086 |

Rotas de documentação (`/api/*/v3/api-docs`) são roteadas sem passar pelo `JwtAuthFilter`.

### Endpoints públicos (sem JWT) dentro de rotas protegidas

O `JwtAuthFilter` aplica lógica extra de bypass para endpoints que precisam ser públicos mesmo dentro de uma rota protegida:

| Endpoint | Condição |
|---|---|
| `GET /api/catalog/establishments/{uuid}` | UUID válido na URL |
| `GET /api/catalog/professionals/{uuid}` | UUID válido na URL |
| `GET /api/catalog/establishments/{uuid}/affiliations/professional/{uuid}/schedule` | UUID válido |
| `GET /api/bookings/availability` | Exato |
| `GET /api/reviews/professional/{uuid}` e `/stats` | UUID válido |
| `GET /api/reviews/establishment/{uuid}` e `/stats` | UUID válido |

---

## Validação JWT

O `JwtAuthFilter` é um `AbstractGatewayFilterFactory` — aplicado por rota no YAML de configuração.

**Fluxo de validação:**

```
Authorization: Bearer <token>
        │
        ▼
JwtValidationService.validateTokenAndGetClaims(token)
        │  carrega RSA public key (PEM) via RSA_PUBLIC_KEY_PATH ou RSA_PUBLIC_KEY
        │  Jwts.parserBuilder().setSigningKey(publicKey).parseClaimsJws(token)
        │  valida: assinatura RS256, expiração, formato
        ▼
Claims extraídos do token:
  sub   → X-User-Id    (UUID do usuário)
  role  → X-User-Role  (ex: ROLE_OWNER)
  email → X-User-Email (e-mail do usuário)
        │
        ▼
Request encaminhado ao serviço destino com os três headers injetados
```

Os serviços downstream confiam nesses headers sem re-validar o JWT — a responsabilidade de autenticação fica inteiramente no gateway.

**Erros:**

| Situação | HTTP Status |
|---|---|
| Header `Authorization` ausente | 401 |
| Header não começa com `Bearer ` | 401 |
| Token com assinatura inválida ou expirado | 401 |
| Chave pública não configurada | 500 |

---

## Swagger UI agregada

Disponível em `http://localhost:8080/swagger-ui.html`. O `GlobalFilterConfig` registra os grupos do SpringDoc apontando para `/api/<serviço>/v3/api-docs` de cada serviço.

> O search-service usa GraphQL e não tem Swagger. Acesse o GraphiQL em `http://localhost:8085/graphiql`.

---

## CORS

Configurado globalmente via `spring.cloud.gateway.globalcors`:

```yaml
allowedOriginPatterns: ${CORS_ALLOWED_ORIGINS:*}
allowedMethods: [GET, POST, PUT, DELETE, OPTIONS]
allowedHeaders: "*"
allowCredentials: true
```

Em produção, defina `CORS_ALLOWED_ORIGINS` com os domínios permitidos.

---

## Variáveis de ambiente

| Variável | Default (local) | Descrição |
|---|---|---|
| `AUTH_SERVICE_URI` | `http://localhost:8081` | URI do auth-service |
| `CATALOG_SERVICE_URI` | `http://localhost:8083` | URI do catalog-service |
| `BOOKING_SERVICE_URI` | `http://localhost:8082` | URI do booking-service |
| `SEARCH_SERVICE_URI` | `http://localhost:8085` | URI do search-service |
| `NOTIFICATION_SERVICE_URI` | `http://localhost:8086` | URI do notification-service |
| `RSA_PUBLIC_KEY_PATH` | — | Path do arquivo PEM da chave pública RSA (ex: `file:/app/certs/public_key.pem`) |
| `RSA_PUBLIC_KEY` | — | Conteúdo PEM inline (alternativa ao path) |
| `CORS_ALLOWED_ORIGINS` | `*` | Origens permitidas para CORS |
| `GATEWAY_PORT` | `8080` | Porta HTTP do gateway |

---

## Testes

| Classe | Tipo | Cenários |
|---|---|---|
| `JwtValidationServiceTest` | Unit | Token válido, token expirado, assinatura inválida, chave não configurada |
| `JwtAuthFilterTest` | Unit | Bypass para rotas públicas, headers injetados corretamente, 401 em token ausente/inválido |
| `GlobalFilterConfigTest` | Unit | Registro dos grupos Swagger |
| `GatewayRoutingTest` | Integration | Roteamento para cada serviço |
| `CucumberTest` | BDD | Fluxo completo de autenticação e roteamento |

```bash
mvn test -pl api-gateway
```

---

## Executar localmente

```bash
# Requer todos os serviços rodando em localhost nas portas padrão
# e infra/certs/public_key.pem presente

mvn spring-boot:run -pl api-gateway
# Swagger UI: http://localhost:8080/swagger-ui.html
```
