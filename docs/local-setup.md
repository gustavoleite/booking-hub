# Subindo a infraestrutura

Este guia cobre duas formas de executar o Booking Hub: via Docker Compose (recomendado) e com os serviços rodando diretamente na JVM para desenvolvimento local.

---

## Pré-requisitos

| Ferramenta | Versão mínima | Necessária em |
|---|---|---|
| Docker + Docker Compose | 24+ | Opção 1 (Docker) |
| Java (JDK) | 21 | Opção 2 (local) |
| Maven | 3.9+ | Opção 2 (local) |
| PostgreSQL | 16 | Opção 2 (local) |
| RabbitMQ | 3 | Opção 2 (local) |
| Elasticsearch | 8.13 | Opção 2 (local) |

---

## Opção 1 — Docker Compose (recomendado)

Sobe toda a stack — infraestrutura e todos os microsserviços — com um único comando. As imagens são construídas via multi-stage build, sem precisar de Maven instalado localmente.

### 1. Clonar e entrar na raiz do repositório

```bash
git clone https://github.com/gustavoleite/booking-hub.git
cd booking-hub
```

### 2. Subir a stack

```bash
docker compose up -d --build
```

Na primeira execução o build das imagens leva alguns minutos. Nas seguintes é muito mais rápido pois as camadas ficam em cache.

### 3. Acompanhar a inicialização

```bash
docker compose ps          # estado de cada container
docker compose logs -f     # logs em tempo real (Ctrl+C para sair)
```

Aguarde todos os serviços com health check estarem `healthy` antes de testar. Isso leva cerca de 60 segundos na primeira vez.

### 4. Verificar se está tudo no ar

```bash
# Gateway — deve retornar 200 com o JSON do Swagger
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/swagger-ui.html

# Elasticsearch
curl -s http://localhost:9200/_cluster/health | grep '"status":"green"\|"status":"yellow"'

# RabbitMQ
curl -s -u guest:guest http://localhost:15672/api/overview | grep '"product_name"'
```

### 5. Parar a stack

```bash
docker compose down           # para os containers, preserva volumes
docker compose down -v        # para e apaga todos os volumes (reset completo)
```

---

## URLs de acesso (Docker ou local)

| Serviço | URL | Observação |
|---|---|---|
| API Gateway | http://localhost:8080 | Único ponto de entrada para os clientes |
| Swagger unificado | http://localhost:8080/swagger-ui.html | Documenta todos os serviços |
| Auth Service (direto) | http://localhost:8081 | |
| Booking Service (direto) | http://localhost:8082 | |
| Catalog Service (direto) | http://localhost:8083 | |
| Search Service (direto) | http://localhost:8085 | |
| Notification Service (direto) | http://localhost:8086 | |
| GraphiQL (search) | http://localhost:8085/graphiql | IDE GraphQL interativa |
| RabbitMQ Management | http://localhost:15672 | `guest` / `guest` |
| MailHog (e-mails dev) | http://localhost:8025 | Captura e-mails sem entregá-los |
| Elasticsearch | http://localhost:9200 | |

> Em produção, todo o tráfego deve passar pelo gateway na porta 8080. Acesso direto às portas dos serviços só é válido em desenvolvimento.

---

## Opção 2 — Execução local (IntelliJ / linha de comando)

Use esta opção quando precisar debugar um serviço específico ou iterar rapidamente sem rebuildar imagens Docker.

### 1. Subir apenas a infraestrutura via Docker

O `docker-compose.yml` inclui um profile `infra` que sobe somente os serviços de infraestrutura:

```bash
docker compose up -d postgres rabbitmq elasticsearch mailhog
```

Aguarde os health checks passarem:

```bash
docker compose ps
```

### 2. Criar os bancos de dados

Na primeira vez, os bancos são criados automaticamente pelo script `infra/init-scripts/init.sql` quando o container do PostgreSQL sobe. Se precisar recriar manualmente:

```bash
docker exec -it bw-postgres psql -U admin -c "
  CREATE DATABASE auth_db;
  CREATE DATABASE catalog_db;
  CREATE DATABASE booking_db;
  CREATE DATABASE notification_db;
"
```

### 3. Gerar as chaves RSA (se não existirem)

As chaves ficam em `infra/certs/`. Se o diretório estiver vazio:

```bash
mkdir -p infra/certs

# Gerar chave privada (usada pelo auth-service para assinar JWTs)
openssl genrsa -out infra/certs/private_key.pem 2048

# Extrair chave pública (usada pelo api-gateway para verificar JWTs)
openssl rsa -in infra/certs/private_key.pem -pubout -out infra/certs/public_key.pem
```

### 4. Iniciar os serviços na ordem correta

O perfil `local` usa `localhost` para todos os recursos de infraestrutura (postgres, rabbitmq, elasticsearch).

```bash
# Terminal 1
cd auth-service && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 2
cd catalog-service && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 3 (aguardar catalog subir antes)
cd booking-service && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 4
cd search-service && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 5
cd notification-service && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 6 (por último — depende de todos os anteriores)
cd api-gateway && mvn spring-boot:run -Dspring-boot.run.profiles=local
```

> As variáveis de ambiente com fallback (`${DB_HOST:localhost}`) garantem que o perfil `local` funciona sem nenhuma configuração adicional, desde que os recursos de infra estejam nas portas padrão.

---

## Variáveis de ambiente relevantes

Todas têm valores padrão que funcionam para desenvolvimento local. Para sobrescrever, exporte antes de rodar:

| Variável | Padrão | Serviço |
|---|---|---|
| `DB_HOST` | `localhost` | auth, catalog, booking, notification |
| `DB_PORT` | `5432` | auth, catalog, booking, notification |
| `DB_USER` / `DB_PASS` | `admin` / `admin123` | auth, catalog, booking, notification |
| `RABBIT_HOST` | `localhost` | catalog, booking, search, notification |
| `RABBITMQ_PORT` | `5672` | catalog, booking, search, notification |
| `ELASTICSEARCH_HOST` | `localhost` | search |
| `ELASTICSEARCH_PORT` | `9200` | search |
| `RSA_PRIVATE_KEY_PATH` | `file:infra/certs/private_key.pem` | auth |
| `RSA_PUBLIC_KEY_PATH` | `file:infra/certs/public_key.pem` | gateway |
| `MAIL_HOST` / `MAIL_PORT` | `localhost` / `1025` | notification |
| `NOTIFICATION_BASE_URL` | `http://localhost:8080/api/calendar` | notification |

---

## Rodando os testes

```bash
# Testes unitários de todos os módulos
mvn test

# Testes de um módulo específico
mvn test -pl booking-service

# Testes + relatório de cobertura JaCoCo (target/site/jacoco/index.html)
mvn verify

# Testes BDD (Cucumber) — requer infra de teste rodando
mvn test -pl booking-service -Dtest="CucumberRunner"
```

> Os testes BDD usam Testcontainers e sobem os recursos necessários automaticamente. Não é preciso ter a infraestrutura rodando manualmente para eles.
