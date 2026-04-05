Feature: Create a review

  Background:
    Given a completed booking exists for the client

  Scenario: Client submits a review with both ratings
    When the client submits a review with professionalRating 5 and establishmentRating 4
    Then the response status is 201
    And the review professionalRating is 5
    And the review establishmentRating is 4

  Scenario: Client submits a review with only professional rating
    When the client submits a review with professionalRating 5 and no establishment rating
    Then the response status is 201
    And the review professionalRating is 5

  Scenario: Client cannot review a booking that does not belong to them
    When another client tries to submit a review for the booking
    Then the response status is 403

  Scenario: Client cannot review a booking that is not completed
    When the client submits a review for a non-eligible booking
    Then the response status is 422

  Scenario: Client cannot review the same booking twice
    Given the client has already reviewed the booking
    When the client submits a review with professionalRating 5 and establishmentRating 4
    Then the response status is 409

  Scenario: Review with no ratings is rejected
    When the client submits a review with no ratings
    Then the response status is 400
