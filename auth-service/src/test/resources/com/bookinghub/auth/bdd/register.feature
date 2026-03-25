# language: pt
Funcionalidade: Registro de Usuário com Validações de Segurança

  Cenário: Tentativa de registro com e-mail já existente
    Dado que o e-mail "cliente@teste.com" já está cadastrado no banco de dados
    Quando eu envio uma requisição POST para "/register" com este e-mail
    Então o status da resposta deve ser 409 CONFLICT
    E o corpo da resposta deve conter o título "Conflito de Dados"

  Cenário: Tentativa de registro com senha fraca
    Quando eu envio uma requisição POST para "/register" com a senha "123"
    Então o status da resposta deve ser 400 BAD REQUEST
    E o corpo da resposta deve informar que a senha deve conter no mínimo 8 caracteres

  Cenário: Tentativa de registro com perfil (role) inválida
    Quando eu envio uma requisição POST para "/register" com a role "ROLE_HACKER"
    Então o status da resposta deve ser 400 BAD REQUEST
    E o corpo da resposta deve informar os valores permitidos
