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
