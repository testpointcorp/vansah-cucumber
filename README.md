# Vansah Cucumber Integration (Showcase)

Minimal example of integrating Cucumber with Vansah using the official Java binding `VansahNode` from: [testpointcorp/Vansah-API-Binding-Java](https://github.com/testpointcorp/Vansah-API-Binding-Java).

## Prerequisites

- Java JDK 8+
- Maven 3.6+
- Vansah installed in your Jira workspace
- A Vansah Connect token

## Setup

1. Copy `.env.example` to `.env` and fill in your values.
2. Put your Vansah/Jira test case key in the Cucumber tag `@TC-...` (example: `@TC-TD-C1`).

## Project structure (minimal)

```
src/
├── main/java/com/testpoint/vansah/config/VansahConfig.java
└── test/
    ├── java/com/vansah/VansahNode.java                 # binding ufficiale (copiato dal repo)
    ├── java/com/testpoint/cucumber/hooks/VansahHooks.java
    ├── java/com/testpoint/cucumber/runners/CucumberTestRunner.java
    ├── java/com/testpoint/cucumber/steps/ExampleSteps.java
    └── resources/features/example.feature
```

## Usage

### Run tests

```bash
mvn clean test
```

### Run a specific scenario by tag

```bash
mvn test -Dcucumber.filter.tags="@TC-EXAMPLE-001"
```

## How it works

- `VansahHooks` sends **one result per scenario** (PASS/FAIL) using the official binding “quick test” methods (no step-by-step logging).

## Feature example

```gherkin
@Vansah
Feature: Example Feature

  @TC-EXAMPLE-001
  Scenario: Successful test scenario
    Given I have a test scenario
    When I perform an action
    Then I verify the result
```

## Available methods (from the binding)

`VansahNode` provides (among others):

- `addTestRunFromJIRAIssue(String testcase)`
- `addTestRunFromTestFolder(String testcase)`
- `addTestRunFromAdvancedTestPlan(String assetType, String testCaseKey)`
- `addTestRunFromStandardTestPlan(String testCaseKey)`
- `addTestLog(String result, String comment, Integer testStepRow)`
- `addTestLog(String result, String comment, Integer testStepRow, File screenshot)`

## Troubleshooting

### Token not configured
Make sure your `.env` contains a valid `CONNECT_DEMO_TOKEN` (or `VANSAH_TOKEN` if you prefer).

### Test run not created
Verify the scenario has a `@TC-...` (or `@TESTCASE-...`) tag with a valid test case key.

### API call errors
Check `VANSAH_URL`, `VANSAH_PROJECT_KEY`, and your token.

## License

This project is provided as an example showcase of a Cucumber ↔ Vansah integration.

