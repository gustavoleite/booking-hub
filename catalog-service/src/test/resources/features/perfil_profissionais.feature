# language: pt
Funcionalidade: Gestão do Perfil Público do Profissional
  Como um prestador de serviços (ROLE_PROFESSIONAL)
  Quero poder criar e editar meu perfil público
  Para que os clientes vejam minhas especialidades e biografia

  Contexto:
    Dado que eu me autentico enviando o header "X-User-Id" com o valor "777e4567-e89b-12d3-a456-426614174777"

  Cenário: Criar o perfil do profissional com sucesso
    Quando eu envio uma requisição POST para "/professionals/me" informando o nome "Maria Manicure" e especialidade "Unhas"
    Então o status da resposta deve ser 201 CREATED

  Cenário: Atualizar o perfil do profissional com sucesso
    Dado que o profissional "777e4567-e89b-12d3-a456-426614174777" possui o nome "Maria Manicure" salvo no banco
    Quando eu envio uma requisição PUT para "/professionals/me" informando o nome "Maria Nail Art" e especialidade "Desenho em Unhas"
    Então o status da resposta deve ser 200 OK

  Cenário: Falha ao tentar criar perfil sem informar o nome
    Quando eu envio uma requisição POST para "/professionals/me" informando a bio mas com o nome vazio
    Então o status da resposta deve ser 400 BAD REQUEST
    E o corpo da resposta deve pedir a obrigatoriedade do nome

  Cenário: Consulta pública do perfil do profissional
    Dado que o profissional "777e4567-e89b-12d3-a456-426614174777" possui o nome "Maria Manicure" salvo no banco
    Quando qualquer usuário envia uma requisição GET para "/professionals/777e4567-e89b-12d3-a456-426614174777" sem enviar header de autenticação
    Então o status da resposta deve ser 200 OK
    E o corpo da resposta deve conter o nome "Maria Manicure"

  Cenário: Consulta pública de um profissional inexistente
    Quando qualquer usuário envia uma requisição GET para "/professionals/00000000-0000-0000-0000-000000000000"
    Então o status da resposta deve ser 404 NOT FOUND
