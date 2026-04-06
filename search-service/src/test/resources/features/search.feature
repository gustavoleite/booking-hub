Feature: Buscar estabelecimentos via GraphQL

  Scenario: Busca por cidade retorna estabelecimentos indexados
    Given um estabelecimento "Salão da Maria" em "São Paulo" está indexado
    When uma query GraphQL busca estabelecimentos com city "São Paulo"
    Then a resposta contém ao menos 1 resultado
    And o resultado inclui o nome "Salão da Maria"

  Scenario: Filtro por rating mínimo exclui estabelecimentos abaixo do threshold
    Given dois estabelecimentos indexados com ratings 3.0 e 4.5
    When uma query GraphQL filtra por minRating 4.0
    Then apenas o estabelecimento com rating 4.5 aparece nos resultados

  Scenario: Busca por nome de profissional retorna estabelecimento onde ele atua
    Given o profissional "João Cabeleireiro" está afiliado ao "Salão do João"
    When uma query busca establishments com query "João Cabeleireiro"
    Then o "Salão do João" aparece nos resultados

  Scenario: Busca por texto livre retorna estabelecimentos pelo nome
    Given um estabelecimento "Barbearia do Carlos" em "Campinas" está indexado
    When uma query GraphQL busca por texto "Barbearia do Carlos"
    Then a resposta contém ao menos 1 resultado
    And o resultado inclui o nome "Barbearia do Carlos"

  Scenario: Filtro por faixa de preço retorna apenas estabelecimentos dentro do range
    Given um estabelecimento indexado com preço mínimo 20.0 e máximo 50.0
    And um estabelecimento indexado com preço mínimo 100.0 e máximo 200.0
    When uma query GraphQL filtra por maxPrice 60.0
    Then o resultado não inclui estabelecimentos com minPrice acima de 60.0

  Scenario: Busca por geolocalização retorna estabelecimentos no raio informado
    Given um estabelecimento indexado em lat -23.5505 lon -46.6333
    When uma query GraphQL busca por geo lat -23.5505 lon -46.6333 raio 1.0
    Then a resposta contém ao menos 1 resultado

  Scenario: Filtro por serviços retorna estabelecimentos que oferecem o serviço
    Given um estabelecimento "Barbearia Estilo" em "São Paulo" com serviço "Corte Masculino" está indexado
    When uma query GraphQL filtra por serviço "Corte Masculino"
    Then a resposta contém ao menos 1 resultado
    And o resultado inclui o nome "Barbearia Estilo"

  Scenario: Paginação retorna apenas a quantidade solicitada por página
    Given 5 estabelecimentos indexados no estado "RJ"
    When uma query GraphQL busca no estado "RJ" com page 0 e size 3
    Then a resposta retorna 3 resultados na página
    And totalHits é ao menos 5
