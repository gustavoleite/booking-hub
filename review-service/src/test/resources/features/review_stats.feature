Feature: Review statistics

  Scenario: Get professional stats with no reviews
    When a request is made for professional stats with no reviews
    Then the response status is 200
    And the totalReviews is 0
    And the averageRating is null

  Scenario: Get establishment stats with no reviews
    When a request is made for establishment stats with no reviews
    Then the response status is 200
    And the totalReviews is 0
    And the averageRating is null

  Scenario: Get professional stats after reviews are submitted
    Given a completed booking exists for the client
    And the client has already reviewed the booking
    When a request is made for professional stats
    Then the response status is 200
    And the totalReviews is 1
