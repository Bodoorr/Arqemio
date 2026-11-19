Feature: Project Security

  Scenario: Access projects without a token
    When I request projects without a token
    Then the response status should be 403

  Scenario: Access projects with an invalid token
    When I request projects with an invalid token
    Then the response status should be 403
