# 🏥 Catalog Service (Management of Establishments & Professionals)

Este microsserviço é responsável pelo gerenciamento de estabelecimentos, profissionais e serviços dentro do sistema Booking Hub. Desenvolvido sob os princípios da **Clean Architecture**, ele permite o cadastro de locais de atendimento, perfis profissionais e a oferta de serviços, além de disponibilizar integrações via mensageria.

## 🚀 Tecnologias Utilizadas
* **Java 21** / **Spring Boot 3.x**
* **Spring Data JPA** (Persistência relacional)
* **PostgreSQL** (Banco de dados principal)
* **Flyway** (Migrations de Banco de Dados)
* **RabbitMQ** (Mensageria assíncrona para eventos — serialização em JSON via `Jackson2JsonMessageConverter`)
* **Springdoc OpenAPI** (Documentação Swagger)
* **Cucumber / RestAssured** (Testes de aceitação e BDD)

## ⚙️ Ambientes e Perfis (Profiles)
O projeto utiliza **Maven Profiles** para gerenciar diferentes ambientes de execução, ajustando automaticamente as configurações de banco de dados e mensageria.

### 1. Profile `local` (Desenvolvimento Direto)
*   **Propósito:** Execução na máquina do desenvolvedor.
*   **Banco de Dados:** Aponta para `localhost:5432`.
*   **RabbitMQ:** Aponta para `localhost:5672`.
*   **Como rodar:**
    ```bash
    ./mvnw spring-boot:run -Plocal
    ```
    *(Nota: Como o profile `local` é o padrão no `pom.xml`, o comando `./mvnw spring-boot:run` sozinho também funciona).*

### 2. Profile `docker` (Containers e Nuvem/AWS)
*   **Propósito:** Empacotamento para Docker e implantação em ambientes orquestrados.
*   **Banco de Dados:** Utiliza o host `postgres` para resolução via DNS interno do Docker.
*   **RabbitMQ:** Utiliza o host `rabbitmq`.
*   **Uso em Produção:** O profile é ativado via variável de ambiente `SPRING_PROFILES_ACTIVE=docker`.

## 📁 Estrutura Clean Architecture
O código segue uma divisão rigorosa para manter a lógica de negócio independente de frameworks:
- `core/`:
    - `domain/`: Entidades de negócio puras.
    - `usecases/`: Regras de negócio (Casos de Uso). **Coberto por testes unitários e TDD.**
    - `ports/`: Interfaces de entrada e saída.
- `infrastructure/`:
    - `adapters/in/rest/`: Controladores REST (Exposição da API).
    - `adapters/out/database/`: Persistência via Spring Data JPA.
    - `adapters/out/messaging/`: Publicação de eventos via RabbitMQ (JSON).
    - `configuration/`: Definição de Beans e configurações do Spring (incluindo `RabbitMQConfig`).

## 🔐 Controle de Acesso e Roles
O acesso aos endpoints é controlado via **API Gateway**, que valida o JWT e encaminha os cabeçalhos `X-User-Id` e `X-User-Role` para o serviço.

| Recurso | Método | Endpoint | Permissão (Role) | Descrição |
| :--- | :---: | :--- | :--- | :--- |
| **Estabelecimentos** | `POST` | `/api/catalog/establishments` | `ROLE_OWNER` | Criar um novo salão |
| | `GET` | `/api/catalog/establishments/my-salons` | `ROLE_OWNER` | Listar salões do proprietário |
| | `GET` | `/api/catalog/establishments/{id}` | Público | Ver detalhes de um salão |
| | `PUT` | `/api/catalog/establishments/{id}` | `ROLE_OWNER` | Atualizar dados do salão |
| | `DELETE` | `/api/catalog/establishments/{id}` | `ROLE_OWNER` | Inativar um salão (soft delete) |
| | `POST` | `/api/catalog/establishments/{id}/services` | `ROLE_OWNER` | Adicionar serviço ao catálogo (aditivo) |
| **Profissionais** | `POST` | `/api/catalog/professionals/me` | `ROLE_PROFESSIONAL` | Criar perfil do profissional |
| | `PUT` | `/api/catalog/professionals/me` | `ROLE_PROFESSIONAL` | Atualizar perfil do profissional |
| | `GET` | `/api/catalog/professionals/me` | `ROLE_PROFESSIONAL` | Ver meu perfil profissional |
| | `GET` | `/api/catalog/professionals/{id}` | Público | Ver perfil de um profissional |
| **Afiliações** | `POST` | `/api/catalog/establishments/{id}/affiliations` | `ROLE_OWNER` | Vincular profissional ao salão (upsert) |
| | `GET` | `/api/catalog/establishments/{id}/affiliations/professional/{profId}/schedule` | Público | Consultar agenda e preço do serviço |

## 📝 Regras de Negócio Relevantes

### Estabelecimentos
- CNPJ é validado pelo algoritmo oficial dos dígitos verificadores. Pontuação é removida automaticamente.
- Ao menos um serviço (`services`) deve ser fornecido na criação.
- **`businessHours`** são substituídos integralmente no `PUT`. A operação usa `DELETE` + `INSERT` com flush garantido pela anotação `@Transactional` no adapter.
- **`services`** nunca são removidos automaticamente (operação aditiva), pois podem estar referenciados por afiliações.

### Afiliações
- O `workSchedule` do profissional deve estar dentro dos `businessHours` do estabelecimento para o dia correspondente.
- Não é possível ter horários sobrepostos para o mesmo dia.
- `POST /affiliations` tem comportamento de **upsert**: se já existir uma afiliação para o par (estabelecimento, profissional), ela é atualizada em vez de criar um novo registro.

### Mensageria (RabbitMQ)

Exchange: `catalog.events` (topic, durable). Todos os eventos são serializados em **JSON** via `Jackson2JsonMessageConverter`.

| Routing Key | Quando publicado |
|---|---|
| `establishment.created` | `POST /catalog/establishments` com sucesso |
| `establishment.updated` | `PUT /catalog/establishments/{id}` com sucesso |
| `affiliation.created` | Nova afiliação profissional criada |
| `affiliation.updated` | Afiliação existente atualizada (preços, horários ou status) |

Os payloads de afiliação incluem `professionalName`, `professionalSpecialties` e `serviceOfferings[].serviceTitle` — enriquecidos para consumo pelo `search-service` sem chamadas síncronas adicionais.

### Campos obrigatórios no endereço

A partir da versão atual, os campos `latitude` e `longitude` são **obrigatórios** no `AddressDto` de criação e atualização de estabelecimentos (`@NotNull`). Isso garante que 100% dos documentos indexados no `search-service` tenham coordenadas geográficas válidas. Os campos `city` e `state` também fazem parte do payload de endereço.

## ⚙️ Justificativa de Stack

| Tecnologia | Justificativa |
|---|---|
| **Spring Data JPA + PostgreSQL** | Modelo relacional adequado para o domínio estrutural do catálogo (estabelecimentos, profissionais, serviços e afiliações têm relacionamentos complexos com integridade referencial) |
| **Flyway** | Migrations versionadas garantem reprodutibilidade em múltiplos ambientes (local, docker, CI, produção) |
| **RabbitMQ (Spring AMQP)** | Desacopla o catalog-service do search-service. Eventos publicados em topic exchange permitem que novos consumidores sejam adicionados sem alterar o produtor |
| **Jackson2JsonMessageConverter** | Serialização JSON das mensagens RabbitMQ — mais legível e debugável do que Java serialization; compatível com consumidores em qualquer linguagem |

## 🔧 Variáveis de Ambiente

| Variável | Default (local) | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `catalog_db` | Nome do banco |
| `DB_USER` | `admin` | Usuário do banco |
| `DB_PASS` | `admin123` | Senha do banco |
| `RABBIT_HOST` | `localhost` | Host do RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Porta AMQP |
| `RABBITMQ_USER` | `guest` | Usuário RabbitMQ |
| `RABBITMQ_PASSWORD` | `guest` | Senha RabbitMQ |

## 🧪 Testes

```bash
# Todos os testes
mvn test -pl catalog-service

# Apenas BDD (Cucumber + REST Assured)
mvn test -pl catalog-service -Dtest="CucumberTest"

# Com relatório de cobertura JaCoCo
mvn verify -pl catalog-service
```

Cobertura mínima: **80% de linhas**. Relatório em `target/site/jacoco/index.html`.

Excluídos da contagem: entidades JPA (`*Entity`), DTOs, classes de configuração.

## 📚 Documentação da API (Swagger)
A documentação dos endpoints REST pode ser acessada em:
`http://localhost:8083/swagger-ui.html` (ou agregada no API Gateway em `http://localhost:8080/swagger-ui.html`)
