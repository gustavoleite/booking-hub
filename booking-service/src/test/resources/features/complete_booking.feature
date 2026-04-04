Feature: Complete a booking

  Background:
    Given the catalog returns an active schedule for the next weekday
    And a confirmed booking exists for the client

  Scenario: Professional marks a booking as completed
    When the professional completes the booking
    Then the response status is 200
    And the booking status is "COMPLETED"

  Scenario: Owner marks a booking as completed
    When the owner completes the booking
    Then the response status is 200
    And the booking status is "COMPLETED"

  Scenario: Client cannot complete a booking
    When the client tries to complete the booking
    Then the response status is 403

  Scenario: Cannot complete an already cancelled booking
    Given the client has already cancelled the booking
    When the professional tries to complete the booking
    Then the response status is 422
