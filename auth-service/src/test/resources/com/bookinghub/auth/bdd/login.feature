# language: pt
Funcionalidade: Autenticacao de Usuario
  Como um usuario cadastrado
  Quero fazer login com meu email e senha
  Para obter um token de acesso aos servicos

  Cenario: Login bem-sucedido com credenciais validas
    Dado que existe um usuario com email "cliente@teste.com" e senha "SenhaForte123!"
    Quando eu envio uma requisicao POST para "/login" com email "cliente@teste.com" e senha "SenhaForte123!"
    Entao o status da resposta deve ser 200 OK
    E o corpo da resposta deve conter um "accessToken" valido

  Cenario: Tentativa de login com senha errada
    Dado que existe um usuario com email "cliente2@teste.com" e senha "SenhaForte123!"
    Quando eu envio uma requisicao POST para "/login" com email "cliente2@teste.com" e senha "SenhaErrada123"
    Entao o status da resposta deve ser 401 UNAUTHORIZED
    E o corpo da resposta deve conter o titulo "Falha na Autenticação"

  Cenario: Tentativa de login com usuario inexistente
    Quando eu envio uma requisicao POST para "/login" com email "inexistente@teste.com" e senha "SenhaQualquer123"
    Entao o status da resposta deve ser 401 UNAUTHORIZED
    E o corpo da resposta deve conter o titulo "Falha na Autenticação"

  Cenario: Tentativa de login com usuario inativo
    Dado que existe um usuario inativo com email "inativo@teste.com" e senha "SenhaForte123!"
    Quando eu envio uma requisicao POST para "/login" com email "inativo@teste.com" e senha "SenhaForte123!"
    Entao o status da resposta deve ser 403 FORBIDDEN
    E o corpo da resposta deve conter o titulo "Usuário Inativo"

  Cenario: Tentativa de login com e-mail em formato invalido
    Quando eu envio uma requisicao POST para "/login" com email "email_invalido" e senha "SenhaForte123!"
    Entao o status da resposta deve ser 400 BAD REQUEST
    E o corpo da resposta deve conter o titulo "Erro de Validação"
