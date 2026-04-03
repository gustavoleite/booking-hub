Feature: Check professional availability

  Scenario: Retrieve available slots for a professional who works on the requested day
    Given the professional works on the requested day
    When a client requests available slots
    Then the response status is 200
    And the response contains available slots

  Scenario: No available slots when professional does not work on the requested day
    Given the professional does not work on the requested day
    When a client requests available slots
    Then the response status is 200
    And the response contains an empty list of available slots
