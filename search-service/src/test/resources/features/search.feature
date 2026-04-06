Feature: Buscar estabelecimentos

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
