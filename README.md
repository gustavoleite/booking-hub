# 💇‍♀️ Booking HUB - Sistema de Agendamento distribuído

Bem-vindo ao repositório central do **Beauty & Wellness**, um sistema robusto, escalável e de alta disponibilidade para agendamento e gerenciamento de serviços de beleza e bem-estar.

Este projeto foi desenvolvido como requisito do Tech Challenge (Fase 3), aplicando conceitos avançados de Arquitetura de Software, Clean Architecture, Microsserviços e Cloud Native.

---

## 🏗️ Visão Geral da Arquitetura

O sistema foi desenhado sob uma arquitetura de **Microsserviços Event-Driven**, garantindo escalabilidade independente, tolerância a falhas e separação clara de domínios (Bounded Contexts). 

Optamos por uma abordagem de **Persistência Poliglota**, utilizando o banco de dados mais adequado para o padrão de acesso de cada microsserviço (Relacional, Documentos, Motor de Busca e Chave-Valor). Toda a comunicação com clientes externos é centralizada por um **API Gateway**, que atua como *Edge Service* e validador de segurança (Stateless JWT).

### Padrões de Comunicação
- **Externa (Cliente ↔ Gateway):** RESTful (JSON) sobre HTTPS.
- **Interna Síncrona (Serviço ↔ Serviço):** `gRPC` para chamadas de baixa latência e alta performance (ex: validação de disponibilidade no momento do agendamento).
- **Interna Assíncrona (Event-Driven):** Mensageria com `RabbitMQ` para desacoplamento, Coreografia de Sagas e atualização de bases de leitura (Padrão CQRS).

---

## 🗺️ Diagrama de Arquitetura

Abaixo está o fluxo macro de comunicação, segurança e dados do ecossistema:

```mermaid
flowchart TB
    %% Estilos visuais
    classDef user fill:#08427b,stroke:#052e56,color:#fff,stroke-width:2px
    classDef gateway fill:#1168bd,stroke:#0b4884,color:#fff,stroke-width:2px
    classDef service fill:#23a2d9,stroke:#18739b,color:#fff,stroke-width:2px,rx:10,ry:10
    classDef db fill:#438dd5,stroke:#2f6395,color:#fff,stroke-width:2px
    classDef broker fill:#f2a900,stroke:#b37d00,color:#fff,stroke-width:2px
    classDef external fill:#7f8c8d,stroke:#2c3e50,color:#fff,stroke-width:2px

    %% Atores
    Client["Postman / API Client\n(REST / JSON)"]:::user

    %% Camada de Borda
    subgraph Edge["Camada de Borda / Entrada"]
        direction TB
        GW["API Gateway\n(Spring Cloud Gateway)"]:::gateway
    end

    %% Barramento de Eventos
    subgraph EventBus["Mensageria / Event-Driven"]
        RabbitMQ{{"RabbitMQ\n(Event Bus)"}}:::broker
    end

    %% Microsserviços
    subgraph Services ["Microsserviços (Spring Boot + Clean Architecture)"]
        direction TB
        Auth["Auth Service\n(IAM / Segurança)"]:::service
        Catalog["Catalog Service\n(Estabelecimentos/Serviços)"]:::service
        Booking["Booking Service\n(Agendamentos - Core)"]:::service
        Review["Review Service\n(Avaliações)"]:::service
        Search["Search Service\n(Busca / CQRS)"]:::service
        Notify["Notification Service\n(Alertas / Sincronização)"]:::service
    end

    %% Bancos de Dados
    subgraph Data["Databases (Persistência Poliglota)"]
        direction TB
        DB_Auth[("PostgreSQL\n(Users/Roles)")]:::db
        DB_Catalog[("PostgreSQL\n(Salões/Profs)")]:::db
        DB_Booking[("PostgreSQL\n(Appointments)")]:::db
        DB_Review[("MongoDB\n(Comments/Ratings)")]:::db
        DB_Search[("Elasticsearch\n(Índices/Geo)")]:::db
        DB_Notify[("Redis\n(Cache/Retries)")]:::db
    end

    %% Integrações Externas
    ExtCal["Google Calendar / Outlook\n(APIs Externas)"]:::external

    %% --- CONEXÕES ---
    Client ===|"HTTPS / REST"| GW
    GW -->|"Roteamento REST\n(Valida JWT RS256)"| Auth
    GW -->|"REST"| Catalog
    GW -->|"REST"| Booking
    GW -->|"REST"| Review
    GW -->|"REST"| Search

    Booking -.->|"gRPC\n(Valida Profissional/Serviço)"| Catalog

    Catalog -.->|"Publica Evento\n(CatalogUpdated)"| RabbitMQ
    Booking -.->|"Publica Evento\n(BookingCreated/Cancelled)"| RabbitMQ
    Review -.->|"Publica Evento\n(ReviewCreated)"| RabbitMQ

    RabbitMQ ==>|"Consome\n(Atualiza Índice/CQRS)"| Search
    RabbitMQ ==>|"Consome\n(Dispara Notificações)"| Notify

    Auth --- DB_Auth
    Catalog --- DB_Catalog
    Booking --- DB_Booking
    Review --- DB_Review
    Search --- DB_Search
    Notify --- DB_Notify
    Notify -->|"OAuth2 Sync"| ExtCal
```

---

## 🧩 Ecossistema de Componentes

### 1. API Gateway (`api-gateway`)
Ponto único de entrada (Spring Cloud Gateway). Responsável pelo roteamento dinâmico e pela **validação de segurança** (verificação da assinatura matemática da chave pública RS256 do JWT), repassando a identidade do usuário para os serviços de *backend* via *Headers* sem sobrecarregá-los com regras de IAM.

### 2. Auth Service (`auth-service`)
Provedor de Identidade (IdP). Responsável por registrar usuários, validar credenciais (e-mail/senha com BCrypt) e emitir os *JSON Web Tokens* (JWT) usando uma chave privada RSA. Protege rigorosamente os dados de acesso no seu próprio PostgreSQL.

### 3. Catalog Service (`catalog-service`)
Gerencia o domínio de negócios estruturais: cadastro de Estabelecimentos, Profissionais associados, Horários de Funcionamento e Serviços (com preço e duração).

### 4. Booking Service (`booking-service`)
O "coração" do sistema. Aplica regras rígidas de concorrência no banco de dados relacional (PostgreSQL) para evitar *double-booking* (agendamentos duplicados no mesmo horário). Comunica-se via `gRPC` com o catálogo para consultas ultrarrápidas de disponibilidade.

### 5. Review Service (`review-service`)
Coleta notas e comentários após a finalização de um serviço. Utiliza MongoDB pela flexibilidade de esquema na persistência de avaliações em texto livre.

### 6. Search Service (`search-service`)
Motor de busca e descoberta altamente otimizado. Implementa o padrão **CQRS** (Command Query Responsibility Segregation) escutando eventos do RabbitMQ para construir um índice consolidado no **Elasticsearch**, permitindo buscas ultrarrápidas por geolocalização, serviços oferecidos e melhor avaliação.

### 7. Notification Service (`notification-service`)
Microsserviço puramente reativo/orientado a eventos. Escuta o barramento do RabbitMQ para disparar e-mails/SMS de confirmação e realizar a integração (sincronização) com APIs externas, como o Google Calendar.

---

## 🛠️ Stack Tecnológica Base

* **Linguagem:** Java 21
* **Framework Principal:** Spring Boot 3.x / Spring Cloud
* **Arquitetura de Código:** Clean Architecture (Domain, Application, Infrastructure)
* **Comunicação:** RESTful (Spring Web), gRPC, RabbitMQ (Spring AMQP)
* **Bancos de Dados:** PostgreSQL (Relacional), MongoDB (NoSQL Documento), Elasticsearch (Busca), Redis (Cache)
* **Segurança:** Spring Security, OAuth2 / JWT (RS256 Criptografia Assimétrica), BCrypt
* **Qualidade e Testes:** JUnit 5, Mockito, Testcontainers, Cucumber (BDD), k6 (Performance)
* **Infraestrutura/DevOps:** Docker, Docker Compose, GitHub Actions (CI/CD), AWS ECS (Deploy)

---

## 📁 Estrutura do Monorepo

O projeto segue a estrutura de monorepo multi-módulo (via Maven ou Gradle), centralizando a infraestrutura, mas mantendo o isolamento de código de cada serviço:

```text
beauty-wellness-system/
 ├── .github/workflows/       # Pipelines de CI/CD
 ├── infra/                   # Arquivos globais de infra
 │    ├── docker-compose.yml  # Sobe BDs, RabbitMQ e Gateway locais
 │    └── grafana/            # Dashboards de observabilidade
 │
 ├── api-gateway/             # Roteamento e Filtro JWT
 ├── auth-service/            # Emissão de Tokens e IAM
 ├── catalog-service/         # Clean Architecture (Core, Application, Infra)
 ├── booking-service/         # Clean Architecture (Core, Application, Infra)
 ├── review-service/          # Clean Architecture (Core, Application, Infra)
 ├── search-service/          # Microsserviço de indexação
 └── notification-service/    # Worker assíncrono
```

---

## 🚀 Como Executar Localmente

*(Instruções a serem adicionadas após o setup do Docker Compose)*
1. Certifique-se de ter o Docker e o Docker Compose instalados.
2. Na raiz do projeto, execute: `docker compose -f infra/docker-compose.yml up -d` para subir todos os bancos de dados e mensageria.
3. Inicie os microsserviços via IDE ou linha de comando através dos respectivos profiles de ambiente (`application-local.yml`).
```