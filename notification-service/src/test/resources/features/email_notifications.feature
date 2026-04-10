Feature: Email Notifications for Booking Events

  Scenario: Client and professional receive confirmation email when booking is created
    Given a booking event is created for client "client@test.com" and professional "pro@test.com" at "2026-08-10T09:00:00"
    When the booking created event is processed
    Then a confirmation email should be sent to "client@test.com"
    And a confirmation email should be sent to "pro@test.com"

  Scenario: Client and professional receive cancellation email when booking is cancelled
    Given an existing confirmed booking snapshot for client "client@test.com" and professional "pro@test.com"
    When the booking cancelled event is processed
    Then a cancellation email should be sent to "client@test.com"
    And a cancellation email should be sent to "pro@test.com"

  Scenario: Only the client receives an email when booking is completed
    Given an existing confirmed booking snapshot for client "client@test.com" and professional "pro@test.com"
    When the booking completed event is processed
    Then a completed email should be sent to "client@test.com"

  Scenario: No reminder emails are sent when there are no bookings in the upcoming window
    Given there are no bookings due in the next 24 hours
    When the reminder job runs
    Then no reminder emails should be sent
