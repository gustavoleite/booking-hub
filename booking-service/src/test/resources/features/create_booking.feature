Feature: Create a booking

  Background:
    Given the catalog returns an active schedule for the next weekday

  Scenario: Client creates a booking successfully
    When a client creates a booking for the next weekday at 10:00
    Then the response status is 201
    And the booking status is "CONFIRMED"
    And the booking price is 80.0

  Scenario: Client cannot double-book an already taken slot
    Given a booking already exists for the next weekday at 10:00
    When a client creates a booking for the next weekday at 10:00
    Then the response status is 409

  Scenario: Client cannot book outside professional working hours
    When a client creates a booking for the next weekday at 20:00
    Then the response status is 409

  Scenario: Client cannot book a past datetime
    When a client creates a booking with a past datetime
    Then the response status is 400
