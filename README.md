# Vansah Cucumber Java

Complete integration of Cucumber with Vansah Test Management using the official Java binding `VansahNode` from: [testpointcorp/Vansah-API-Binding-Java](https://github.com/testpointcorp/Vansah-API-Binding-Java).

## Features

| Feature | Description |
|---------|-------------|
| **Quick Test Mode** | One result per scenario (default) |
| **Step-Level Reporting** | Detailed results for each Cucumber step |
| **Screenshot on Failure** | Automatic screenshot capture when steps fail |
| **Test Folder Integration** | Link tests to Vansah test folders |
| **Jira Issue Integration** | Link tests to Jira issues |
| **Advanced Test Plan** | Execute tests within Advanced Test Plans |
| **Standard Test Plan** | Execute tests within Standard Test Plans |
| **Test Run Properties** | Attach Sprint, Release, Environment metadata |

## Prerequisites

- Java JDK 8+
- Maven 3.6+
- Vansah installed in your Jira workspace
- A Vansah Connect token

## Quick Start

1. Copy `.env.example` to `.env` and configure your values
2. Tag your scenarios with `@TC-{testCaseKey}` (e.g., `@TC-PROJ-C1`)
3. Run tests: `mvn clean test`

## Configuration

### Required Settings

| Variable | Description |
|----------|-------------|
| `CONNECT_DEMO_TOKEN` | Vansah API token |
| `VANSAH_PROJECT_KEY` | Jira project key (e.g., "PROJ") |

### Integration Context (choose at least one)

| Variable | Description |
|----------|-------------|
| `TEST_FOLDER_PATH` | Test folder path (e.g., "Automation/Cucumber") |
| `JIRA_ISSUE_KEY` | Jira issue key to link tests |
| `ADVANCED_TEST_PLAN_KEY` | Advanced Test Plan key |
| `STANDARD_TEST_PLAN_KEY` | Standard Test Plan key |

### Feature Flags

| Variable | Default | Description |
|----------|---------|-------------|
| `STEP_LEVEL_REPORTING` | `false` | Enable per-step reporting |
| `SCREENSHOT_ON_FAILURE` | `false` | Capture screenshots on failure |

### Test Run Properties (optional)

| Variable | Description |
|----------|-------------|
| `SPRINT_NAME` | Sprint name for test runs |
| `RELEASE_NAME` | Release/version name |
| `ENVIRONMENT_NAME` | Environment (e.g., "UAT", "PROD") |

## Usage

### Run all tests

```bash
mvn clean test
```

### Run specific scenario by tag

```bash
mvn test -Dcucumber.filter.tags="@TC-PROJ-C1"
```

### Enable step-level reporting

```bash
STEP_LEVEL_REPORTING=true mvn clean test
```

## Reporting Modes

### Quick Test Mode (default)

Sends **one result per scenario**. Fast and simple.

```
✅ Quick test result sent: PASSED
```

### Step-Level Reporting Mode

Sends **individual results for each step**. More detailed but more API calls.

Enable with `STEP_LEVEL_REPORTING=true`:

```
🚀 Test run created (Test Folder: Automation/Cucumber)
📝 Step 1 logged: PASSED
📝 Step 2 logged: PASSED
📝 Step 3 logged: FAILED
📸 Screenshot attached for step 3
✅ Scenario completed: FAILED (steps logged individually)
```

## Integration Priority

When multiple integration contexts are configured, the priority is:

1. **Advanced Test Plan** (with Test Folder or Jira Issue as asset)
2. **Standard Test Plan**
3. **Test Folder**
4. **Jira Issue**

## Feature File Example

```gherkin
@Vansah
Feature: Example Feature for Vansah Integration

  @TC-PROJ-C1
  Scenario: Successful test scenario
    Given I have a test scenario
    When I perform an action
    Then I verify the result

  @TC-PROJ-C2
  Scenario: Another test scenario
    Given I have another scenario
    When I perform a different action
    Then I verify the different result
```

## Project Structure

```
src/
├── main/java/com/testpoint/vansah/config/
│   └── VansahConfig.java          # Configuration loader
└── test/
    ├── java/com/vansah/
    │   └── VansahNode.java        # Official Vansah binding
    ├── java/com/testpoint/cucumber/
    │   ├── hooks/VansahHooks.java # Cucumber hooks for Vansah
    │   ├── runners/CucumberTestRunner.java
    │   └── steps/ExampleSteps.java
    └── resources/features/
        └── example.feature
```

## Available VansahNode Methods

### Test Run Creation

| Method | Description |
|--------|-------------|
| `addTestRunFromTestFolder(testCase)` | Create run linked to test folder |
| `addTestRunFromJIRAIssue(testCase)` | Create run linked to Jira issue |
| `addTestRunFromAdvancedTestPlan(assetType, testCase)` | Create run in Advanced Test Plan |
| `addTestRunFromStandardTestPlan(testCase)` | Create run in Standard Test Plan |

### Quick Tests (single result per scenario)

| Method | Description |
|--------|-------------|
| `addQuickTestFromTestFolders(testCase, result)` | Quick test with folder |
| `addQuickTestFromJiraIssue(testCase, result)` | Quick test with Jira issue |

### Step-Level Logging

| Method | Description |
|--------|-------------|
| `addTestLog(result, comment, stepRow)` | Log step result |
| `addTestLog(result, comment, stepRow, screenshot)` | Log step with screenshot |

### Result Codes

| Code | Meaning |
|------|---------|
| 0 | N/A |
| 1 | FAILED |
| 2 | PASSED |
| 3 | UNTESTED |

## Troubleshooting

### Token not configured
Ensure `.env` contains a valid `CONNECT_DEMO_TOKEN`.

### Test run not created
Verify the scenario has a `@TC-...` or `@TESTCASE-...` tag with a valid test case key.

### API call errors
Check `VANSAH_URL`, `VANSAH_PROJECT_KEY`, and your token.

### No integration context
Configure at least one of: `TEST_FOLDER_PATH`, `JIRA_ISSUE_KEY`, `ADVANCED_TEST_PLAN_KEY`, or `STANDARD_TEST_PLAN_KEY`.

### Invalid folder path
Folder paths must:
- NOT start with `/`
- Contain at least one `/` (e.g., "Root/Subfolder")

## License

This project is provided as an example showcase of a Cucumber ↔ Vansah integration.
