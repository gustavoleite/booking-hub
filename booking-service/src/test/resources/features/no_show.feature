Feature: Mark booking as no-show

  Background:
    Given the catalog returns an active schedule for the next weekday
    And a confirmed booking exists for the client

  Scenario: Professional marks a booking as no-show
    When the professional marks the booking as no-show
    Then the response status is 200
    And the booking status is "NO_SHOW"

  Scenario: Owner marks a booking as no-show
    When the owner marks the booking as no-show
    Then the response status is 200
    And the booking status is "NO_SHOW"

  Scenario: Client cannot mark a no-show
    When the client tries to mark the booking as no-show
    Then the response status is 403

  Scenario: Cannot mark no-show on an already cancelled booking
    Given the client has already cancelled the booking
    When the professional marks the booking as no-show
    Then the response status is 422
