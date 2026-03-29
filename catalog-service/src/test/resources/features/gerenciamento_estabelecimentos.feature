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
    E o corpo da resposta deve conter a mensagem sobre "Close time must be after open time"

  Cenário: Listagem dos meus estabelecimentos
    Dado que eu tenho 2 salões cadastrados no meu "X-User-Id"
    Quando eu envio uma requisição GET para "/api/catalog/establishments/my-salons"
    Então o status da resposta deve ser 200 OK
    E a lista de resposta deve conter exatamente 2 itens

  Cenário: Bloqueio de edição de um salão de outro dono
    Dado que existe um salão com ID "salao-456" que pertence ao usuário "owner-999"
    Quando eu envio uma requisição PUT para "/api/catalog/establishments/salao-456"
    Então o status da resposta deve ser 403 FORBIDDEN
