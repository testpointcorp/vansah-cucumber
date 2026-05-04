# Auto-generated Cucumber feature file from Vansah
# Project: CUC
# Generated: 2026-04-24T02:17:50.511Z
# Total Scenarios: 8

Feature: Issue: CUC-1
  Exported test cases from Vansah

  @CUC-C6
  @version:v1
  @type:functional
  @priority:medium
  Scenario: Verify that the calculator correctly performs addition for two numeric inputs
    # Precondition: The calculator application is running and ready to accept input.
    Given the calculator is initialized
    And the user enters the first numeric input
    And the user enters the second numeric input
    When the user selects the addition operation
    Then the calculator returns the correct sum of the two inputs

  @CUC-C7
  @version:v1
  @type:functional
  @priority:medium
  Scenario: Verify that the calculator correctly performs subtraction for two numeric inputs
    # Precondition: The calculator application is running and ready to accept input.
    Given the calculator is initialized
    And the user enters the first numeric input
    And the user enters the second numeric input
    When the user selects the subtraction operation
    Then the calculator returns the correct difference of the two inputs

  @CUC-C8
  @version:v1
  @type:functional
  @priority:medium
  Scenario: Verify that the calculator correctly performs multiplication for two numeric inputs
    # Precondition: The calculator application is running and ready to accept input.
    Given the calculator is initialized
    And the user enters the first numeric input
    And the user enters the second numeric input
    When the user selects the multiplication operation
    Then the calculator returns the correct product of the two inputs

  @CUC-C9
  @version:v1
  @type:functional
  @priority:medium
  Scenario: Verify that the calculator correctly performs division for two numeric inputs
    # Precondition: The calculator application is running and ready to accept input.
    Given the calculator is initialized
    And the user enters the first numeric input
    And the user enters the second numeric input (not zero)
    When the user selects the division operation
    Then the calculator returns the correct quotient of the two inputs

  @CUC-C10
  @version:v1
  @type:functional
  @priority:medium
  Scenario: Verify that the calculator handles division by zero gracefully and returns an appropriate error or exception
    # Precondition: The calculator application is running and ready to accept input.
    Given the calculator is initialized
    And the user enters a numeric input as the dividend
    And the user enters zero as the divisor
    When the user selects the division operation
    Then the calculator returns an appropriate error message or exception indicating division by zero is not allowed

  @CUC-C11
  @version:v1
  @type:functional
  @priority:medium
  Scenario: Verify that the calculator accepts two numeric inputs for all operations
    # Precondition: The calculator application is running and ready to accept input.
    Given the calculator is initialized
    When the user attempts to enter two numeric inputs for any operation
    Then the calculator accepts both numeric inputs without error

  @CUC-C12
  @version:v1
  @type:functional
  @priority:medium
  Scenario: Verify that the user can select each arithmetic operation (+, -, *, /) and receive the correct result
    # Precondition: The calculator application is running and ready to accept input.
    Given the calculator is initialized
    And the user enters two numeric inputs
    When the user selects each arithmetic operation (+, -, *, /) in turn
    Then the calculator returns the correct result for each selected operation

  @CUC-C13
  @version:v1
  @type:functional
  @priority:medium
  Scenario: Verify that the calculator returns the correct result for each arithmetic operation
    # Precondition: The calculator application is running and ready to accept input.
    Given the calculator is initialized
    And the user enters two valid numeric inputs
    When the user selects any arithmetic operation (+, -, *, /) except division by zero
    Then the calculator returns the correct result for the selected operation
