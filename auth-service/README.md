# Auth Service

Provedor de identidade (IdP) do Booking Hub. Responsável por registrar usuários, validar credenciais e emitir JWTs assinados com RSA (RS256). Implementado com Clean Architecture — a camada de domínio não tem nenhuma dependência de frameworks.

---

## Stack e justificativa

| Tecnologia | Justificativa |
|---|---|
| **Spring Boot 3.2 / Java 21** | Base do ecossistema do projeto |
| **Spring Security** | Gerencia o contexto de segurança e integra com o BCryptPasswordEncoder. Não usa autenticação stateful — apenas para hashing e configuração de filtros HTTP |
| **Nimbus JOSE + JWT** | Biblioteca de referência para operações criptográficas com JWTs (RS256). Escolhida sobre JJWT por ser mais completa em operações de assinatura RSA e ser a mesma usada internamente pelo Spring Authorization Server |
| **BCrypt** | Algoritmo de hashing adaptativo para senhas. O fator de custo garante resistência a ataques de força bruta mesmo com hardware atual |
| **PostgreSQL + Flyway** | Banco dedicado (`auth_db`), isolado dos demais serviços. Flyway garante migrations versionadas e reproduzíveis |
| **SpringDoc OpenAPI** | Documentação Swagger agregada pela API Gateway |
| **Testcontainers** | Testes de integração com PostgreSQL real (não H2) para garantir fidedignidade das queries JPA |

---

## Arquitetura interna (Clean Architecture)

```
core/
  domain/
    User           ← entidade raiz: id (UUID), email, senha (hash), roles, active
    Role           ← enum: ROLE_CLIENT, ROLE_PROFESSIONAL, ROLE_OWNER
    Credential     ← value object interno para autenticação
  ports/
    UserRepository      ← interface de persistência
    TokenGenerator      ← interface de geração de token
    PasswordEncoder     ← interface de hashing
  usecases/
    RegisterUserUseCase      ← valida e-mail único, força da senha e role; persiste com hash BCrypt
    AuthenticateUserUseCase  ← valida credenciais; retorna JWT assinado
    GetUserEmailUseCase      ← busca e-mail por userId (usado pelo booking-service)
  exceptions/
    EmailAlreadyExistsException   (409)
    InvalidCredentialsException   (401)
    InactiveUserException         (403)
    WeakPasswordException         (400)
    InvalidRoleException          (400)

application/dto/
  RegisterRequestDTO    { email, password, role }
  LoginRequestDTO       { email, password }
  TokenResponseDTO      { accessToken, expiresIn: 3600, tokenType: "Bearer" }
  UserResponseDTO       { id, email }

infrastructure/
  adapters/in/rest/
    AuthController           ← POST /register, POST /login
    InternalUserController   ← GET /internal/users/{id}/email (sem autenticação pública)
    GlobalExceptionHandler   ← mapeia exceções de domínio para ProblemDetail (RFC 9457)
  adapters/out/database/
    UserEntity                        ← @Entity JPA
    JpaUserRepository                 ← Spring Data JPA
    PostgresUserRepositoryAdapter     ← implementa UserRepository
  adapters/out/security/
    BCryptPasswordEncoderAdapter      ← implementa PasswordEncoder
  adapters/out/jwt/
    NimbusTokenGeneratorAdapter       ← implementa TokenGenerator; carrega RSA private key (PEM);
                                        cacheia a chave parseada; gera fallback se key não configurada
  configuration/
    BeanConfig        ← wiring manual dos use cases com os adaptadores (sem @Autowired no core)
    SecurityConfig    ← desabilita CSRF, CORS permissivo, todos os endpoints liberados (a autenticação é no gateway)
    OpenApiConfig     ← configura o Swagger
```

---

## API REST

Porta padrão: **8081**. Via API Gateway: `/api/auth/...`

### `POST /register`

Cria um novo usuário. Roles aceitas: `ROLE_CLIENT`, `ROLE_PROFESSIONAL`, `ROLE_OWNER`.

**Request:**
```json
{ "email": "user@email.com", "password": "Senha123!", "role": "ROLE_CLIENT" }
```

**Response 201:**
```json
{ "id": "uuid", "email": "user@email.com" }
```

**Erros:** `400` (senha fraca ou role inválida), `409` (e-mail já cadastrado)

**Regras de senha:** mínimo 8 caracteres, ao menos uma letra maiúscula, uma minúscula e um dígito.

---

### `POST /login`

Autentica e retorna JWT.

**Request:**
```json
{ "email": "user@email.com", "password": "Senha123!" }
```

**Response 200:**
```json
{ "accessToken": "eyJ...", "expiresIn": 3600, "tokenType": "Bearer" }
```

**Erros:** `401` (credenciais inválidas), `403` (usuário inativo)

---

### `GET /internal/users/{id}/email`

Endpoint interno — não exposto via API Gateway. Usado pelo `booking-service` para enriquecer eventos RabbitMQ com o e-mail do profissional.

**Response 200:**
```json
{ "email": "prof@salon.com" }
```

**Response 404:** usuário não encontrado.

---

## Estrutura do JWT

O token é assinado com RS256 usando a chave privada RSA do auth-service. A chave pública correspondente é usada pelo API Gateway para validação.

```json
Header: { "alg": "RS256" }
Payload: {
  "sub":  "<userId UUID>",
  "email": "user@email.com",
  "role":  "ROLE_CLIENT",
  "iat":  <unix timestamp>,
  "exp":  <iat + 3600>
}
```

Expiração: **1 hora** (3600 segundos). Não há refresh token.

---

## Variáveis de ambiente

| Variável | Default (local) | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `auth_db` | Nome do banco |
| `DB_USER` | `admin` | Usuário do banco |
| `DB_PASS` | `admin123` | Senha do banco |
| `RSA_PRIVATE_KEY_PATH` | — | Path do arquivo PEM da chave privada (ex: `file:/app/certs/private_key.pem`) |
| `RSA_PRIVATE_KEY` | — | Conteúdo PEM inline (alternativa ao path) |
| `AUTH_SERVICE_PORT` | `8081` | Porta HTTP do serviço |

> Se nenhuma chave privada for configurada, o serviço gera um par RSA efêmero em memória. Útil para testes rápidos, mas os tokens não sobrevivem a reinicializações.

---

## Testes

```bash
# Todos os testes
mvn test -pl auth-service

# Apenas BDD (Cucumber + Spring Boot + Testcontainers)
mvn test -pl auth-service -Dtest="CucumberTest"

# Apenas integração JPA
mvn test -pl auth-service -Dtest="JpaUserRepositoryIT"

# Com relatório de cobertura
mvn verify -pl auth-service
```

| Classe | Tipo | Cenários |
|---|---|---|
| `UserTest` | Unit | Construção, validações de domínio |
| `CredentialTest` | Unit | Value object de autenticação |
| `RoleTest` | Unit | Enum de roles válidas |
| `RegisterUserUseCaseTest` | Unit | Happy path, e-mail duplicado, senha fraca, role inválida |
| `AuthenticateUserUseCaseTest` | Unit | Happy path, credenciais inválidas, usuário inativo |
| `BCryptPasswordEncoderAdapterTest` | Unit | Encoding e matching |
| `NimbusTokenGeneratorAdapterTest` | Unit | Token gerado, claims corretos, chave efêmera |
| `PostgresUserRepositoryAdapterTest` | Unit | Adapter com mock do JPA repository |
| `AuthControllerTest` | Unit | Controller com mock dos use cases |
| `GlobalExceptionHandlerTest` | Unit | Mapeamento de exceções para HTTP |
| `JpaUserRepositoryIT` | Integration | CRUD real no PostgreSQL via Testcontainers |
| `CucumberTest` | BDD | Registro e login com Spring Boot + Testcontainers |

Cobertura mínima: **80% de linhas**. Relatório em `target/site/jacoco/index.html`.

---

## Executar localmente

```bash
# Requer PostgreSQL em localhost:5432 com banco auth_db criado
mvn spring-boot:run -pl auth-service

# Swagger UI: http://localhost:8081/swagger-ui.html
# Via gateway: http://localhost:8080/swagger-ui.html
```
