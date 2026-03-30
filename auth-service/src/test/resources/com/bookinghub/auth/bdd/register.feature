# language: pt
Funcionalidade: Registro de Usuário com Validações de Segurança
  Como um novo usuário
  Quero me cadastrar no sistema
  Para poder acessar as funcionalidades do Booking HUB

  Cenário: Registro de usuário com sucesso
    Quando eu envio uma requisição POST para "/register" com email "novo_usuario@teste.com", senha "SenhaForte123!" e role "ROLE_CLIENT"
    Então o status da resposta deve ser 201 CREATED
    E o corpo da resposta deve conter o id do usuário e o email "novo_usuario@teste.com"

  Cenário: Tentativa de registro com e-mail já existente
    Dado que o e-mail "cliente@teste.com" já está cadastrado no banco de dados
    Quando eu envio uma requisição POST para "/register" com este e-mail
    Então o status da resposta deve ser 409 CONFLICT
    E o corpo da resposta deve conter o titulo "Conflito de Dados"

  Cenário: Tentativa de registro com senha fraca
    Quando eu envio uma requisição POST para "/register" com a senha "123"
    Então o status da resposta deve ser 400 BAD REQUEST
    E o corpo da resposta deve informar que a senha é fraca

  Cenário: Tentativa de registro com perfil (role) inválida
    Quando eu envio uma requisição POST para "/register" com a role "ROLE_HACKER"
    Então o status da resposta deve ser 400 BAD REQUEST
    E o corpo da resposta deve informar os valores permitidos

  Cenário: Tentativa de registro com e-mail inválido
    Quando eu envio uma requisição POST para "/register" com email "email_invalido", senha "SenhaForte123!" e role "ROLE_CLIENT"
    Então o status da resposta deve ser 400 BAD REQUEST
    E o corpo da resposta deve conter o titulo "Erro de Validação"
