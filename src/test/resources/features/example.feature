# Auto-generated Cucumber feature file from Vansah
# Project: ATP
# Generated: 2026-01-12T12:08:43.203Z
# Total Scenarios: 3

Feature: Folder: Calculator basic operations
  Exported test cases from Vansah

  @ATP-C409
  @version:v1
  @type:functional
  @priority:high
  @label:regression
  Scenario: Verify that the calculator correctly adds two numbers (10 and 5) and returns 15 as the result
    # Precondition: The calculator application is installed and accessible. The user is on the calculator's main screen and the calculator is ready to accept input.
    Given I have two numbers 10 and 5
    When I add the numbers
    Then the result should be 15

  @ATP-C410
  @version:v1
  @type:functional
  @priority:high
  @label:regression
  Scenario: Validate that the calculator correctly subtracts two numbers (20 and 8) and returns 12 as the result
    # Precondition: The calculator application is installed and accessible. The user is on the calculator's main screen and the calculator is ready to accept input.
    Given I have two numbers 20 and 8
    When I subtract the numbers
    Then the result should be 12

  @ATP-C411
  @version:v1
  @type:functional
  @priority:highest
  @label:regression
  Scenario: Test that the calculator returns the correct result when dividing two numbers (10 and 2), ensuring the output is not an incorrect value such as 6
    # Precondition: The calculator application is installed and accessible. The user is on the calculator's main screen and the calculator is ready to accept input.
    Given I have two numbers 10 and 2
    When I divide the numbers
    Then the result should be 6

