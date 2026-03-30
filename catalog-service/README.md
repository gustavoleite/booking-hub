# 🏥 Catalog Service (Management of Establishments & Professionals)

Este microsserviço é responsável pelo gerenciamento de estabelecimentos, profissionais e serviços dentro do sistema Booking Hub. Desenvolvido sob os princípios da **Clean Architecture**, ele permite o cadastro de locais de atendimento, perfis profissionais e a oferta de serviços, além de disponibilizar integrações via gRPC e mensageria.

## 🚀 Tecnologias Utilizadas
* **Java 21** / **Spring Boot 3.x**
* **Spring Data JPA** (Persistência relacional)
* **PostgreSQL** (Banco de dados principal)
* **Flyway** (Migrations de Banco de Dados)
* **gRPC** (Comunicação inter-serviços de alta performance)
* **RabbitMQ** (Mensageria assíncrona para eventos)
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
*   **Banco de Dados:** Utiliza o host `catalog-db` para resolução via DNS interno do Docker.
*   **RabbitMQ:** Utiliza o host `rabbitmq`.
*   **Como rodar via Maven (apenas para teste de build):**
    ```bash
    ./mvnw spring-boot:run -Pdocker
    ```
*   **Uso em Produção (AWS):** O profile é ativado via variável de ambiente `SPRING_PROFILES_ACTIVE=docker`. Configurações sensíveis devem ser injetadas via variáveis de ambiente ou Secrets Manager.

## 📁 Estrutura Clean Architecture
O código segue uma divisão rigorosa para manter a lógica de negócio independente de frameworks:
- `core/`: 
    - `domain/`: Entidades de negócio puras.
    - `usecases/`: Regras de negócio (Casos de Uso). **Coberto por testes unitários e TDD.**
    - `ports/`: Interfaces de entrada e saída.
- `infrastructure/`: 
    - `adapters/in/rest/`: Controladores REST (Exposição da API).
    - `adapters/in/grpc/`: Servidores gRPC para integração interna.
    - `adapters/out/database/`: Persistência via Spring Data JPA.
    - `adapters/out/messaging/`: Publicação de eventos via RabbitMQ.
    - `configuration/`: Definição de Beans e configurações do Spring.

## 🔐 Controle de Acesso e Roles
O acesso aos endpoints é controlado via **API Gateway**, que valida o JWT e encaminha os cabeçalhos `X-User-Id` e `X-User-Role` para o serviço.

| Recurso | Método | Endpoint | Permissão (Role) | Descrição |
| :--- | :---: | :--- | :--- | :--- |
| **Estabelecimentos** | `POST` | `/api/catalog/establishments` | `ROLE_OWNER` | Criar um novo salão |
| | `GET` | `/api/catalog/establishments/my-salons` | `ROLE_OWNER` | Listar salões do proprietário |
| | `GET` | `/api/catalog/establishments/{id}` | `Público` | Ver detalhes de um salão |
| | `PUT` | `/api/catalog/establishments/{id}` | `ROLE_OWNER` | Atualizar dados do salão |
| | `DELETE` | `/api/catalog/establishments/{id}` | `ROLE_OWNER` | Inativar um salão |
| | `POST` | `/api/catalog/establishments/{id}/services` | `ROLE_OWNER` | Adicionar serviço ao catálogo |
| **Profissionais** | `PUT` | `/api/catalog/professionals/me` | `ROLE_PROFESSIONAL` | Criar/Atualizar perfil do profissional |
| | `GET` | `/api/catalog/professionals/me` | `ROLE_PROFESSIONAL` | Ver meu perfil profissional |
| | `GET` | `/api/catalog/professionals/{id}` | `Público` | Ver perfil de um profissional |
| **Afiliações** | `POST` | `/api/catalog/establishments/{id}/affiliations` | `ROLE_OWNER` | Vincular profissional ao salão |

## 📚 Documentação da API (Swagger)
A documentação dos endpoints REST pode ser acessada em:
👉 `http://localhost:8083/swagger-ui.html`

A definição das mensagens gRPC pode ser encontrada em:
📁 `src/main/proto/`
