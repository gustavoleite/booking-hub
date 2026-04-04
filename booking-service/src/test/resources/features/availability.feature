Feature: Check professional availability

  Scenario: Retrieve available slots when professional works on the requested day
    Given the catalog returns an active schedule for the next weekday
    When a client requests availability for the next weekday
    Then the response status is 200
    And the response contains available slots with duration 60 and price 80.0

  Scenario: No available slots when professional does not work on the requested day
    Given the catalog returns an active schedule with no days configured
    When a client requests availability for the next weekday
    Then the response status is 200
    And the response contains an empty list of available slots

  Scenario: No available slots when the professional is inactive
    Given the catalog returns an inactive schedule
    When a client requests availability for the next weekday
    Then the response status is 200
    And the response contains an empty list of available slots
