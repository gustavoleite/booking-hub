Feature: List reviews

  Background:
    Given a completed booking exists for the client
    And the client has already reviewed the booking

  Scenario: List reviews for a professional
    When a request is made to list reviews for the professional
    Then the response status is 200
    And the response contains at least 1 review

  Scenario: List reviews for an establishment
    When a request is made to list reviews for the establishment
    Then the response status is 200
    And the response contains at least 1 review

  Scenario: Get review by booking id as client
    When the client requests the review by booking id
    Then the response status is 200
    And the review booking id matches

  Scenario: Another client cannot get review by booking id
    When another client requests the review by booking id
    Then the response status is 403

  Scenario: Review not found for unknown booking
    When the client requests the review for a non-existent booking
    Then the response status is 404
