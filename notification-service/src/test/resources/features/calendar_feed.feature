Feature: Calendar Feed Generation

  Scenario: User with bookings receives a valid ICS feed
    Given the user "client-bdd" has a booking from "2026-05-01T10:00:00" to "2026-05-01T11:00:00" with status "CONFIRMED"
    And the user "client-bdd" has a valid feed token "bddtoken123"
    When the user requests the calendar feed
    Then the response should be a valid ICS calendar
    And the ICS should contain a VEVENT for the booking

  Scenario: User with a cancelled booking sees it reflected in the feed
    Given the user "client-bdd2" has a booking from "2026-05-02T14:00:00" to "2026-05-02T15:00:00" with status "CANCELLED"
    And the user "client-bdd2" has a valid feed token "bddtoken456"
    When the user requests the calendar feed
    Then the response should be a valid ICS calendar
    And the ICS should contain STATUS:CANCELLED

  Scenario: Invalid token returns error
    Given no feed token exists for user "unknown-user"
    When the user "unknown-user" requests the feed with token "wrongtoken"
    Then the use case should throw an IllegalArgumentException
