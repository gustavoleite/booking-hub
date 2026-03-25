# 🚪 API Gateway (Edge Service)

O API Gateway é a porta de entrada única e o escudo do sistema distribuído Booking Hub. Desenvolvido de forma não-bloqueante (Reativa) com **Spring Cloud Gateway**, ele centraliza o roteamento, a política de CORS e a **validação de segurança JWT**.

## ⚙️ Ambientes e Perfis (Profiles)
Assim como os microsserviços, o Gateway utiliza **Maven Profiles** para gerenciar o roteamento dinâmico.

### 1. Profile `local` (Desenvolvimento Local)
*   **Propósito:** Encaminhar requisições para `http://localhost:[PORTA]`.
*   **Ideal para:** Quando você está rodando as APIs diretamente pela sua IDE ou terminal fora do Docker.
*   **Como rodar:**
    ```bash
    ./mvnw spring-boot:run -Plocal
    ```

### 2. Profile `docker` (Rede de Containers / Nuvem / AWS)
*   **Propósito:** Encaminhar requisições usando nomes de host do Docker (Ex: `http://auth-service:8081`).
*   **Ideal para:** Deployment em AWS (ECS/EKS) ou execução via `docker-compose`.
*   **Como ativar:** Injetando a variável de ambiente `SPRING_PROFILES_ACTIVE=docker` no container.

## 🚀 Responsabilidades
1. **Roteamento Dinâmico:** Encaminha chamadas (`/api/auth/**`, `/api/bookings/**`) para os microsserviços adequados escondidos na rede interna.
2. **Offloading de Autenticação:** Intercepta todas as rotas protegidas e valida a assinatura matemática e expiração do JWT (via Chave Pública RS256) antes de repassar a requisição, poupando os microsserviços dessa carga.
3. **Agregação de Swagger:** Unifica a documentação OpenAPI de todos os microsserviços em uma única interface gráfica.

## 📚 Swagger UI Centralizado
Para visualizar todos os endpoints do sistema consolidados, acesse:
👉 `http://localhost:8080/swagger-ui.html`
