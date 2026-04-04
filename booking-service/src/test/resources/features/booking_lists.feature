Feature: List bookings

  Background:
    Given the catalog returns an active schedule for the next weekday
    And a confirmed booking exists for the client

  Scenario: Client lists their own bookings
    When the client lists their bookings
    Then the response status is 200
    And the response contains at least 1 booking

  Scenario: Professional lists their agenda
    When the professional lists their agenda
    Then the response status is 200
    And the response contains at least 1 booking

  Scenario: Owner lists establishment bookings
    When the owner lists establishment bookings
    Then the response status is 200
    And the response contains at least 1 booking
