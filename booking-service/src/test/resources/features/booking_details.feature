Feature: Get booking details

  Background:
    Given the catalog returns an active schedule for the next weekday
    And a confirmed booking exists for the client

  Scenario: Client retrieves their own booking
    When the client requests the booking details
    Then the response status is 200
    And the booking id is present in the response

  Scenario: Professional retrieves a booking
    When the professional requests the booking details
    Then the response status is 200
    And the booking id is present in the response

  Scenario: Owner retrieves a booking
    When the owner requests the booking details
    Then the response status is 200
    And the booking id is present in the response

  Scenario: Another client cannot view this booking
    When another client requests the booking details
    Then the response status is 403

  Scenario: Booking not found returns 404
    When the client requests details for a non-existent booking
    Then the response status is 404
