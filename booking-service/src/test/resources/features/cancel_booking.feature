Feature: Cancel a booking

  Background:
    Given the catalog returns an active schedule for the next weekday
    And a confirmed booking exists for the client

  Scenario: Client cancels their own booking
    When the client cancels the booking with reason "Compromisso surgiu"
    Then the response status is 200
    And the booking status is "CANCELLED"
    And the cancel reason is "Compromisso surgiu"

  Scenario: Owner cancels a booking
    When the owner cancels the booking with reason "Estabelecimento fechado"
    Then the response status is 200
    And the booking status is "CANCELLED"

  Scenario: Another client cannot cancel this booking
    When another client tries to cancel the booking
    Then the response status is 403

  Scenario: Professional cannot cancel a booking
    When a professional tries to cancel the booking
    Then the response status is 403

  Scenario: Client cannot cancel an already cancelled booking
    Given the client has already cancelled the booking
    When the client cancels the booking with reason "Tentativa dupla"
    Then the response status is 422
