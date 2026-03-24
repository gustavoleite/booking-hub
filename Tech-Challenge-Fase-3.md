Tech Challenge
O Tech Challenge é o projeto de fase que englobará os conhecimentos obtidos
em todas as disciplinas. É importante atentar-se ao prazo de entrega, pois esta é uma
atividade obrigatória, que vale 60% da nota de todas as disciplinas da fase.
Objetivo:
Desenvolver um sistema robusto e eficiente de agendamento e gerenciamento
para serviços de beleza e bem-estar, aplicando os conceitos avançados de arquitetura
de software, desenvolvimento orientado a testes e práticas de código limpo.
Funcionalidades:
O sistema deve apresentar as seguintes funcionalidades:
1. Cadastro de estabelecimentos:
   a. Permite que prestadores de serviços de beleza e bem-estar cadastrem
   seus estabelecimentos no sistema.
   b. Requer informações como nome do estabelecimento, endereço, serviços
   oferecidos, profissionais disponíveis, horários de funcionamento e fotos.
2. Perfil de profissionais:
   a. Cada profissional pode ter um perfil no sistema com detalhes sobre suas
   especialidades, horários disponíveis e tarifas.
3. Agendamento de serviços:
   a. Os clientes podem visualizar os serviços oferecidos, a disponibilidade dos
   profissionais e fazer agendamentos online para datas e horários
   específicos.
   b. O sistema deve enviar confirmações e lembretes automáticos tanto para o
   cliente quanto para o profissional.
4. Avaliações e comentários:
   a. Após o serviço, clientes podem deixar avaliações e comentários sobre o
   estabelecimento e o profissional, ajudando outros usuários a tomar
   decisões informadas.
5. Busca e filtragem:
   a. Os usuários podem buscar estabelecimentos por nome, localização,
   serviço oferecido ou avaliação.
   b. Filtragem avançada por disponibilidade, faixa de preço e outros critérios
   personalizáveis.
6. Gerenciamento de agendamentos:
   a. Os estabelecimentos têm acesso a um painel de controle onde podem
   gerenciar os agendamentos, ajustando a agenda de acordo com
   cancelamentos, não comparecimentos e reagendamentos.
7. Integração com calendários:
   a. Possibilidade de sincronização dos agendamentos com calendários
   externos (como Google Calendar ou Outlook) para os profissionais e
   clientes.
   Entregáveis:
   Você deverá apresentar um PDF contendo:
   • Link do GitHub: deve ser fornecido o link do repositório GitHub com o código-
   fonte completo do projeto.
   • Documentação Técnica: documentação detalhada do sistema e da API,
   utilizando ferramentas como Javadoc ou Swagger.
   • Relatório Técnico: uma descrição das tecnologias e ferramentas utilizadas, os
   desafios técnicos enfrentados e as soluções adotadas, com ênfase na aplicação
   dos princípios de Clean Architecture e qualidade de software.
   Avaliação:
1. Clean Architecture:
   a. Design Arquitetônico: aplicar a Clean Architecture no backend, definindo
   claramente as entidades, casos de uso, adaptadores e frameworks
   externos.
2. Qualidade de Software:
   a. Testes Unitários (TDD): utilizar Test-Driven Development (TDD) para criar
   o backend, buscando alta cobertura de testes com jUnit ou outras
   ferramentas adequadas.
   b. Testes Integrados e Análise de Código: realizar testes que integram
   diferentes módulos do sistema para verificar a interação entre eles e utilizar
   ferramentas de análise de código estático para garantir a qualidade.
   c. Testes de Integração e Continuous Integration (CI): testar os controllers
   para validar a lógica de negócio e integrar os testes ao CI, adotando
   Behaviour-Driven Development (BDD) para facilitar a comunicação entre
   desenvolvedores e demais stakeholders.
   d. Testes Não Funcionais: incluir testes de performance e carga para
   assegurar que o sistema comporta um volume elevado de agendamentos
   simultâneos sem degradação de performance.
3. Deploy:
   a. Ambientes Diversos de Deploy: implementar o deploy do sistema em
   pelo menos um ambiente, incluindo deploy local para desenvolvimento e
   teste, e em plataformas de nuvem como AWS ECS, Azure e uma opção
   gratuita, ajustando configurações para cada ambiente conforme necessário.