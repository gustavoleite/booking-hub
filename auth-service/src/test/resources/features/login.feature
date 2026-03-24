Funcionalidade: Autenticação de Usuário
  Como um usuário cadastrado
  Quero fazer login com meu email e senha
  Para obter um token de acesso aos serviços

  Cenário: Login bem-sucedido com credenciais válidas
    Dado que existe um usuário com email "cliente@teste.com" e senha "senha123"
    Quando eu envio uma requisição POST para "/api/auth/login" com estas credenciais
    Então o status da resposta deve ser 200 OK
    E o corpo da resposta deve conter um "accessToken" válido
