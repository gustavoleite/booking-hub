# 🔐 Auth Service (Identity & Access Management)

Este microsserviço é o provedor central de identidade do do sistema distribuído Booking Hub. Desenvolvido sob os princípios da **Clean Architecture**, ele é responsável por gerenciar credenciais, validar senhas via hash (BCrypt) e emitir *JSON Web Tokens* (JWT) utilizando criptografia assimétrica (RSA).

## 🚀 Tecnologias Utilizadas
* **Java 21** / **Spring Boot 3.x**
* **Spring Security** (Autenticação Stateless)
* **PostgreSQL** (Persistência relacional isolada)
* **Flyway** (Migrations de Banco de Dados)
* **Nimbus JOSE + JWT** (Geração de tokens RSA)
* **Springdoc OpenAPI** (Documentação Swagger)

## ⚙️ Ambientes e Perfis (Profiles)
O projeto utiliza **Maven Profiles** para gerenciar diferentes ambientes de execução. Isso garante que as configurações de rede (URIs) e banco de dados sejam ajustadas automaticamente.

### 1. Profile `local` (Desenvolvimento Direto)
*   **Propósito:** Execução rápida na máquina do desenvolvedor (fora de containers).
*   **Banco de Dados:** Aponta para `localhost:5432`.
*   **Como rodar:**
    ```bash
    ./mvnw spring-boot:run -Plocal
    ```
    *(Nota: Como o profile `local` é o padrão no `pom.xml`, o comando `./mvnw spring-boot:run` sozinho também funciona).*

### 2. Profile `docker` (Containers e Nuvem/AWS)
*   **Propósito:** Empacotamento para Docker e implantação em ambientes orquestrados (Docker Compose, Kubernetes ou AWS ECS/EKS).
*   **Banco de Dados:** Utiliza o nome do serviço `auth-db` para resolução via DNS interno do Docker.
*   **Como rodar via Maven (apenas para teste de build):**
    ```bash
    ./mvnw spring-boot:run -Pdocker
    ```
*   **Uso em Produção (AWS):** No Docker/AWS, o profile é ativado via variável de ambiente `SPRING_PROFILES_ACTIVE=docker`. As variáveis sensíveis (como senhas e chaves RSA) devem ser injetadas via *Secrets Manager* ou variáveis de ambiente do serviço.

## 📁 Estrutura Clean Architecture
O código está estritamente dividido para garantir independência de frameworks nas regras de negócio:
- `core/`: Entidades puras, Use Cases (Regras de negócio) e Ports (Interfaces). **100% coberto por TDD.**
- `application/`: DTOs e mapeadores.
- `infrastructure/`: Controladores REST, adaptadores do Spring Data JPA, configurações de segurança.

## 📚 Documentação da API (Swagger)
A documentação detalhada dos endpoints, payloads e códigos de erro pode ser acessada diretamente via:
👉 `http://localhost:8081/swagger-ui.html` (ou agregada no API Gateway na porta 8080).
