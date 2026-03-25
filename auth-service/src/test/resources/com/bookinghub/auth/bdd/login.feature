# language: pt
Funcionalidade: Autenticacao de Usuario
  Como um usuario cadastrado
  Quero fazer login com meu email e senha
  Para obter um token de acesso aos servicos

  Cenario: Login bem-sucedido com credenciais validas
    Dado que existe um usuario com email "cliente@teste.com" e senha "senha123"
    Quando eu envio uma requisicao POST para "/login" com estas credenciais
    Entao o status da resposta deve ser 200 OK
    E o corpo da resposta deve conter um "accessToken" valido
