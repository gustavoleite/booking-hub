# Guia de Testes das APIs - Booking Hub

Este guia descreve o roteiro de testes para as APIs do projeto Booking Hub utilizando o Postman.

## 🚀 Pré-requisitos
- Ambiente rodando via Docker (`docker compose up -d`) ou localmente.
- Postman instalado.
- Importar a coleção disponível em: `docs/postman/Postman_Collection.postman_collection.json`.

## 📍 Configuração de Variáveis
As APIs utilizam as seguintes variáveis no Postman:
- `base_url`: `http://localhost:8080` (Endereço do API Gateway).
- `access_token`: Gerado automaticamente após o login.
- `x_user_id`: UUID do usuário (obtido no registro ou login).
- `establishment_id`: UUID do estabelecimento criado.
- `professional_id`: UUID do profissional (geralmente o mesmo `x_user_id` para testes de perfil próprio).
- `service_id`: UUID do serviço criado no estabelecimento.

---

## 🔐 1. Fluxo de Autenticação (Auth Service)

### 1.1 Registrar Usuário
- **Endpoint:** `POST /api/auth/register`
- **Descrição:** Cria uma nova conta.
- **Roles sugeridas:** `ROLE_CLIENT`, `ROLE_PROFESSIONAL`, `ROLE_OWNER`.
- **Dica:** Use `ROLE_OWNER` para testar a criação de estabelecimentos.

### 1.2 Login (Geração de Token)
- **Endpoint:** `POST /api/auth/login`
- **Descrição:** Autentica o usuário e salva o token JWT na variável `access_token`.
- **Nota:** O script de testes no Postman já está configurado para atualizar a variável automaticamente.

---

## 💇‍♂️ 2. Gestão de Profissionais (Catalog Service)

### 2.1 Criar Perfil Profissional
- **Endpoint:** `POST /api/catalog/professionals/me`
- **Headers:** `X-User-Id` (UUID do usuário logado).
- **Descrição:** Cria os dados iniciais do perfil profissional.

### 2.2 Atualizar Perfil Profissional
- **Endpoint:** `PUT /api/catalog/professionals/me`
- **Headers:** `X-User-Id`.
- **Descrição:** Atualiza os dados do perfil, como especialidades e bio.

### 2.3 Obter Meu Perfil
- **Endpoint:** `GET /api/catalog/professionals/me`
- **Headers:** `X-User-Id`.
- **Descrição:** Recupera os dados do perfil profissional do usuário autenticado. Lança erro customizado se não existir.

---

## 🏪 3. Gestão de Estabelecimentos (Catalog Service - Requer ROLE_OWNER)

### 3.1 Criar Novo Estabelecimento
- **Endpoint:** `POST /api/catalog/establishments`
- **Headers:** `X-User-Id`.
- **Descrição:** Cria um salão/barbearia. 
- **Validação:** O CNPJ deve ser válido (ex: `12.345.678/0001-95`).

### 3.2 Listar Meus Estabelecimentos
- **Endpoint:** `GET /api/catalog/establishments/my-salons`
- **Headers:** `X-User-Id`.
- **Descrição:** Retorna todos os estabelecimentos vinculados ao dono logado.

### 3.3 Adicionar Serviço ao Estabelecimento
- **Endpoint:** `POST /api/catalog/establishments/:id/services`
- **Descrição:** Adiciona um tipo de serviço (ex: "Corte de Cabelo") que o estabelecimento oferece.

---

## 🤝 4. Afiliações e Agenda (Catalog Service)

### 4.1 Vincular Profissional a Estabelecimento
- **Endpoint:** `POST /api/catalog/establishments/:establishmentId/affiliations?professionalId={{professional_id}}`
- **Descrição:** Vincula um profissional ao estabelecimento, definindo os serviços que ele presta naquele local, preços e horários de trabalho.
- **Corpo do JSON (Exemplo):**
```json
{
  "serviceOfferings": [
    {
      "providedServiceId": "UUID_DO_SERVICO",
      "price": 75.0,
      "durationMinutes": 45
    }
  ],
  "workSchedules": [
    {
      "dayOfWeek": 1,
      "startTime": "09:00:00",
      "endTime": "18:00:00"
    }
  ],
  "active": true
}
```

### 4.2 Consultar Agenda do Profissional
- **Endpoint:** `GET /api/catalog/establishments/:establishmentId/affiliations/professional/:professionalId/schedule?serviceId={{service_id}}`
- **Descrição:** Retorna os horários disponíveis e o preço do serviço para um profissional específico em um determinado salão.
- **Dica:** O `serviceId` deve ser o UUID de um serviço já cadastrado e oferecido pelo profissional.

---

## 🌐 Gateway e Outros Serviços
As chamadas devem ser feitas através do **API Gateway** na porta `8080`. O prefixo da URL indica para qual microsserviço a requisição será roteada:
- `/api/auth/**` -> `auth-service` (Porta interna: 8081)
- `/api/catalog/**` -> `catalog-service` (Porta interna: 8083)

> **Nota:** Rotas para `/api/bookings`, `/api/reviews` e `/api/search` estão reservadas no Gateway mas os serviços ainda não estão implementados nesta versão.

---

## 🧪 Casos de Teste de Erro (Negativos)
1. **CNPJ Inválido:** Tente criar um estabelecimento com CNPJ com menos de 14 dígitos ou dígitos verificadores errados. Esperado: `400 Bad Request`.
2. **Acesso Negado:** Tente listar "Meus Salões" sem o header `X-User-Id`. Esperado: `400` ou `403` (dependendo do filtro do gateway).
3. **E-mail Duplicado:** Tente registrar um usuário com um e-mail já existente. Esperado: `409 Conflict`.
