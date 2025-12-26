Feature: Example Feature for Vansah Integration

  @TC-SCRUM-C1
  Scenario: Successful test scenario
    Given I have a test scenario
    When I perform an action
    Then I verify the result

  @TC-SCRUM-C2
  Scenario: Failed test scenario
    Given I have a failing test
    When I perform an action that fails
    Then I should see an error
