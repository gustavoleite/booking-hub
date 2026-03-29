### PARTE 1: Testes Unitários (Camada Core / TDD)
*Ferramentas: JUnit 5 + Mockito*
*Alvo: Classes no pacote `core/usecases/` e `core/domain/`*

#### 1. `CreateEstablishmentUseCaseTest`
*   🟢 **Cenário de Sucesso:** Deve criar um estabelecimento com sucesso quando os dados forem válidos, chamar o `EstablishmentRepository.save()` e retornar o UUID gerado.
*   🔴 **Falha - CNPJ Duplicado:** Dado um CNPJ que o `repository.existsByCnpj()` retorne `true`, deve lançar `ConflictException("CNPJ já cadastrado")` e não salvar.
*   🔴 **Falha - CNPJ Inválido:** Dado um CNPJ com menos de 14 dígitos ou com letras, deve lançar `BusinessRuleException("CNPJ inválido")`.
*   🔴 **Falha - Horário Inválido (Fechamento antes da Abertura):** Dado um array de `BusinessHours` onde a Segunda-feira tem `openTime = 18:00` e `closeTime = 09:00`, deve lançar `BusinessRuleException("Horário de fechamento não pode ser anterior ao de abertura")`.
*   🔴 **Falha - Falta de Endereço:** Dado um payload sem o objeto `Address`, deve lançar `BusinessRuleException("O endereço é obrigatório")`.

#### 2. `UpdateEstablishmentUseCaseTest`
*   🟢 **Cenário de Sucesso:** Dado um `ownerId` válido que seja o dono do estabelecimento, deve atualizar os dados de texto e chamar o `save()`.
*   🔴 **Falha - Violação de Propriedade (Segurança):** Dado um `Establishment` cujo `ownerId` no banco seja diferente do `ownerId` do usuário fazendo a requisição, deve lançar `ForbiddenException("Você não tem permissão para editar este salão")`.
*   🔴 **Falha - Salão Inexistente:** Se o `repository.findById()` retornar vazio, deve lançar `NotFoundException("Estabelecimento não encontrado")`.

#### 3. `InactivateEstablishmentUseCaseTest`
*   🟢 **Cenário de Sucesso:** Deve buscar o salão, alterar a flag `active` para `false` e chamar o `save()`.
*   🔴 **Falha - Violação de Propriedade:** Se o usuário não for o dono, deve lançar `ForbiddenException`.

#### 4. `UpsertProfessionalProfileUseCaseTest`
*   🟢 **Cenário de Sucesso (Criação):** Quando `repository.findById()` retornar vazio, deve criar uma nova entidade `Professional` com o ID do usuário e salvar.
*   🟢 **Cenário de Sucesso (Atualização):** Quando `repository.findById()` retornar um perfil existente, deve apenas atualizar a `bio`, `avatarUrl` e `specialties`, e salvar.
*   🔴 **Falha - Nome Vazio:** Dado um payload sem o nome do profissional, deve lançar `BusinessRuleException("O nome do profissional é obrigatório")`.

---

### PARTE 2: Testes BDD (Camada Infrastructure / Web)
*Ferramentas: Cucumber + REST Assured (+ Testcontainers implicitamente)*
*Alvo: Integração de ponta a ponta passando pelos Controllers REST.*

Você deve criar os seguintes arquivos `.feature` na pasta `src/test/resources/features/`.

#### Arquivo 1: `gerenciamento_estabelecimentos.feature`

```gherkin
# language: pt
Funcionalidade: Gerenciamento de Estabelecimentos (Salões)
  Como um usuário dono de salão (ROLE_OWNER)
  Quero poder cadastrar, editar e inativar meu estabelecimento
  Para que os clientes possam encontrá-lo e agendar serviços

  Contexto:
    Dado que a API está no ar
    E que eu me autentico enviando o header "X-User-Id" com o valor "owner-123"

  Cenário: Cadastro de estabelecimento com sucesso
    Quando eu envio uma requisição POST para "/api/catalog/establishments" com o CNPJ "12345678000199" e horários válidos
    Então o status da resposta deve ser 201 CREATED
    E o corpo da resposta deve conter o "id" do salão gerado

  Cenário: Tentativa de cadastro com CNPJ já existente
    Dado que já existe um salão salvo no banco com o CNPJ "99999999000199"
    Quando eu envio uma requisição POST para "/api/catalog/establishments" com o mesmo CNPJ "99999999000199"
    Então o status da resposta deve ser 409 CONFLICT
    E o corpo da resposta deve informar que houve "Conflito de Dados"

  Cenário: Tentativa de cadastro com horário de funcionamento inválido
    Quando eu envio uma requisição POST para "/api/catalog/establishments" com o horário de abertura "18:00" e fechamento "09:00"
    Então o status da resposta deve ser 400 BAD REQUEST
    E o corpo da resposta deve conter a mensagem sobre "horário de fechamento"

  Cenário: Listagem dos meus estabelecimentos
    Dado que eu tenho 2 salões cadastrados no meu "X-User-Id"
    Quando eu envio uma requisição GET para "/api/catalog/establishments/my-salons"
    Então o status da resposta deve ser 200 OK
    E a lista de resposta deve conter exatamente 2 itens

  Cenário: Bloqueio de edição de um salão de outro dono
    Dado que existe um salão com ID "salao-456" que pertence ao usuário "owner-999"
    Quando eu envio uma requisição PUT para "/api/catalog/establishments/salao-456"
    Então o status da resposta deve ser 403 FORBIDDEN
```

#### Arquivo 2: `perfil_profissionais.feature`

```gherkin
# language: pt
Funcionalidade: Gestão do Perfil Público do Profissional
  Como um prestador de serviços (ROLE_PROFESSIONAL)
  Quero poder criar e editar meu perfil público
  Para que os clientes vejam minhas especialidades e biografia

  Contexto:
    Dado que eu me autentico enviando o header "X-User-Id" com o valor "prof-777"

  Cenário: Atualizar o perfil do profissional (Upsert) com sucesso
    Quando eu envio uma requisição PUT para "/api/catalog/professionals/me" informando o nome "Maria Manicure" e especialidade "Unhas"
    Então o status da resposta deve ser 200 OK

  Cenário: Falha ao tentar atualizar perfil sem informar o nome
    Quando eu envio uma requisição PUT para "/api/catalog/professionals/me" informando a bio mas com o nome vazio
    Então o status da resposta deve ser 400 BAD REQUEST
    E o corpo da resposta deve pedir a obrigatoriedade do nome

  Cenário: Consulta pública do perfil do profissional
    Dado que o profissional "prof-777" possui o nome "Maria Manicure" salvo no banco
    Quando qualquer usuário envia uma requisição GET para "/api/catalog/professionals/prof-777" sem enviar header de autenticação
    Então o status da resposta deve ser 200 OK
    E o corpo da resposta deve conter o nome "Maria Manicure"

  Cenário: Consulta pública de um profissional inexistente
    Quando qualquer usuário envia uma requisição GET para "/api/catalog/professionals/prof-inexistente"
    Então o status da resposta deve ser 404 NOT FOUND
```

---

### O Segredo para Implementar isso Fácil

Na sua classe de *Step Definitions* do Cucumber (onde você traduz o Gherkin para Java), você fará exatamente isso usando o `RestAssured`:

```java
// Exemplo prático de como o Cucumber vai testar a sua API no Spring Boot
@Quando("eu envio uma requisição GET para {string}")
public void sendGetRequest(String endpoint) {
    response = RestAssured.given()
                .header("X-User-Id", this.currentUserId) // Simula o Gateway
                .when()
                .get(endpoint);
}

@Então("o status da resposta deve ser {int}")
public void checkStatus(int expectedStatus) {
    response.then().statusCode(expectedStatus);
}
```

### 👉 Qual é o próximo passo prático?

Com os testes desenhados, o caminho para codificar o **Catálogo (Fase 1)** está totalmente pavimentado (TDD -> Código -> BDD).

O que você quer que eu faça agora?
1. **Mostrar o Código Java:** Quer que eu gere a implementação de uma dessas classes em Java (ex: `CreateEstablishmentUseCase` com seu teste unitário) para você usar como *template* da Clean Architecture?
2. **Avançar para a RFC-008:** Desenhar a arquitetura e os testes da **Fase 2 do Catálogo** (A ligação complexa de vínculos `Affiliation`, preços e horários dos profissionais nos salões)?