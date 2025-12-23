<p align="center">
  <img src="https://vansah.com/app/logo/vansahjira-logo.svg" alt="Vansah Logo" width="300"/>
</p>

<p align="center">
  The "Vansah Cucumber Java" integration enables seamless Cucumber test execution with automatic result reporting to Vansah Test Management for Jira, using the official Java binding <code>VansahNode</code> from: <a href="https://github.com/testpointcorp/Vansah-API-Binding-Java">testpointcorp/Vansah-API-Binding-Java</a>.
</p>

<p align="center">
  <a href="https://vansah.com"><strong>Website</strong></a> • <a href="https://vansah.com/connect-integrations/"><strong>More Connect Integrations</strong></a>
</p>

---

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [Reporting Modes](#reporting-modes)
- [Project Structure](#project-structure)
- [Methods Overview](#methods-overview)
- [Troubleshooting](#troubleshooting)

## Features

- **Quick Test Mode** – Send one result per Cucumber scenario (default behavior).
- **Step-Level Reporting** – Send detailed results for each individual Cucumber step.
- **Screenshot on Failure** – Automatically capture and attach screenshots when steps fail.
- **Test Folder Integration** – Link test executions to Vansah test folders.
- **Jira Issue Integration** – Link test executions to Jira issues.
- **Advanced Test Plan Support** – Execute tests within Advanced Test Plans (ATP).
- **Standard Test Plan Support** – Execute tests within Standard Test Plans (STP).
- **Test Run Properties** – Attach Sprint, Release, and Environment metadata to test runs.
- **Real-Time Sync** – Results are sent immediately as tests execute, not in batch.

## Prerequisites

- Make sure that **Vansah** is installed in your Jira workspace.
- You need to generate a **Vansah Connect token** to authenticate with Vansah APIs.
- Your project requires **Java JDK 8** or newer.
- **Maven 3.6+** for dependency management and test execution.

## Quick Start

1. Copy `.env.example` to `.env` and configure your values.
2. Tag your Cucumber scenarios with `@TC-{testCaseKey}` (e.g., `@TC-PROJ-C1`).
3. Run tests:

```bash
mvn clean test
```

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

## Usage Examples

### Running Cucumber Tests with Vansah Integration

This integration automatically sends test results to Vansah when you run your Cucumber tests. The `VansahHooks` class handles all communication with the Vansah API.

* **Quick Test Mode using Test Folder Path**

```java
// .env configuration
CONNECT_DEMO_TOKEN=your_vansah_token_here
VANSAH_PROJECT_KEY=PROJ
TEST_FOLDER_PATH=Automation/Cucumber
```

```gherkin
@Vansah
Feature: Login Feature

  @TC-PROJ-C1
  Scenario: Successful login
    Given I am on the login page
    When I enter valid credentials
    Then I should see the dashboard
```

```bash
# Run the test - results automatically sent to Vansah
mvn clean test
```

**Console Output:**
```
✅ Quick test result sent: PASSED
```

* **Step-Level Reporting Mode**

Enable step-level reporting to send individual results for each Cucumber step:

```java
// .env configuration
CONNECT_DEMO_TOKEN=your_vansah_token_here
VANSAH_PROJECT_KEY=PROJ
TEST_FOLDER_PATH=Automation/Cucumber
STEP_LEVEL_REPORTING=true
SCREENSHOT_ON_FAILURE=true
```

```bash
# Run with step-level reporting enabled
STEP_LEVEL_REPORTING=true mvn clean test
```

**Console Output:**
```
🚀 Test run created (Test Folder: Automation/Cucumber)
📝 Step 1 logged: PASSED
📝 Step 2 logged: PASSED
📝 Step 3 logged: FAILED
📸 Screenshot attached for step 3
✅ Scenario completed: FAILED (steps logged individually)
```

* **Sending Results using Advanced Test Plan (ATP)**

```java
// .env configuration
CONNECT_DEMO_TOKEN=your_vansah_token_here
VANSAH_PROJECT_KEY=PROJ
ADVANCED_TEST_PLAN_KEY=PROJ-P17
TEST_FOLDER_PATH=Automation/Cucumber
```

```gherkin
@Vansah
Feature: Regression Tests

  @TC-PROJ-C5
  Scenario: Verify user profile
    Given I am logged in
    When I navigate to profile
    Then I should see my details
```

**Console Output:**
```
🚀 Test run created (Advanced Test Plan: PROJ-P17)
✅ Quick test result sent: PASSED
```

* **Sending Results using Standard Test Plan (STP)**

```java
// .env configuration
CONNECT_DEMO_TOKEN=your_vansah_token_here
VANSAH_PROJECT_KEY=PROJ
STANDARD_TEST_PLAN_KEY=PROJ-P18
```

```gherkin
@Vansah
Feature: Smoke Tests

  @TC-PROJ-C10
  Scenario: Verify homepage loads
    Given I open the browser
    When I navigate to homepage
    Then I should see the welcome message
```

**Console Output:**
```
🚀 Test run created (Standard Test Plan: PROJ-P18)
✅ Quick test result sent: PASSED
```

* **Sending Results linked to a Jira Issue**

```java
// .env configuration
CONNECT_DEMO_TOKEN=your_vansah_token_here
VANSAH_PROJECT_KEY=PROJ
JIRA_ISSUE_KEY=PROJ-123
```

```gherkin
@Vansah
Feature: Bug Verification

  @TC-PROJ-C15
  Scenario: Verify bug fix for PROJ-123
    Given the bug has been fixed
    When I perform the failing action
    Then the issue should be resolved
```

**Console Output:**
```
🚀 Test run created (Jira Issue: PROJ-123)
✅ Quick test result sent: PASSED
```

* **Complete Example with Test Run Properties**

```java
// .env configuration
CONNECT_DEMO_TOKEN=your_vansah_token_here
VANSAH_PROJECT_KEY=PROJ
TEST_FOLDER_PATH=Automation/Cucumber
SPRINT_NAME=Sprint 5
RELEASE_NAME=v2.1.0
ENVIRONMENT_NAME=UAT
STEP_LEVEL_REPORTING=true
```

```gherkin
@Vansah
Feature: E2E Payment Flow

  @TC-PROJ-C20
  Scenario: Complete purchase flow
    Given I have items in my cart
    When I proceed to checkout
    And I enter payment details
    And I confirm the order
    Then I should see order confirmation
```

### Running Tests

```bash
# Run all Vansah-tagged tests
mvn clean test

# Run specific scenario by tag
mvn test -Dcucumber.filter.tags="@TC-PROJ-C1"

# Run with step-level reporting
STEP_LEVEL_REPORTING=true mvn clean test

# Run with environment variables
CONNECT_DEMO_TOKEN=xxx VANSAH_PROJECT_KEY=PROJ mvn clean test
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

### Integration Priority

When multiple integration contexts are configured, the priority is:

1. **Advanced Test Plan** (with Test Folder or Jira Issue as asset)
2. **Standard Test Plan**
3. **Test Folder**
4. **Jira Issue**

## Project Structure

```
src/
├── main/java/com/testpoint/vansah/config/
│   └── VansahConfig.java              # Configuration loader
└── test/
    ├── java/com/vansah/
    │   └── VansahNode.java            # Official Vansah binding
    ├── java/com/testpoint/cucumber/
    │   ├── hooks/VansahHooks.java     # Cucumber hooks for Vansah
    │   ├── runners/CucumberTestRunner.java
    │   └── steps/ExampleSteps.java
    └── resources/features/
        └── example.feature
```

## Methods Overview

The `VansahNode` class provides a comprehensive interface for interacting with Vansah Test Management for Jira. Below is an overview of the key methods used in this integration.

### Test Run Creation

| Method | Description |
|--------|-------------|
| `addTestRunFromTestFolder(testCase)` | Create run linked to test folder |
| `addTestRunFromJIRAIssue(testCase)` | Create run linked to Jira issue |
| `addTestRunFromAdvancedTestPlan(assetType, testCase)` | Create run in Advanced Test Plan |
| `addTestRunFromStandardTestPlan(testCase)` | Create run in Standard Test Plan |

### Quick Tests

| Method | Description |
|--------|-------------|
| `addQuickTestFromTestFolders(testCase, result)` | Quick test with folder context |
| `addQuickTestFromJiraIssue(testCase, result)` | Quick test with Jira issue context |

### Step-Level Logging

| Method | Description |
|--------|-------------|
| `addTestLog(result, comment, stepRow)` | Log step result |
| `addTestLog(result, comment, stepRow, screenshot)` | Log step with screenshot |
| `updateTestLog(result, comment)` | Update existing test log |
| `updateTestLog(result, comment, screenshot)` | Update test log with screenshot |

### Result Codes

| Code | Meaning |
|------|---------|
| 0 | N/A |
| 1 | FAILED |
| 2 | PASSED |
| 3 | UNTESTED |

### Setter Methods

| Method | Description |
|--------|-------------|
| `setVansahToken(token)` | Set API authentication token |
| `setVansahURL(url)` | Set custom Vansah API URL |
| `setProjectKey(key)` | Set Jira project key |
| `setFOLDERPATH(path)` | Set test folder path |
| `setJIRA_ISSUE_KEY(key)` | Set Jira issue key |
| `setSPRINT_NAME(name)` | Set sprint name |
| `setRELEASE_NAME(name)` | Set release name |
| `setENVIRONMENT_NAME(name)` | Set environment name |
| `setAdvancedTestPlanKey(key)` | Set Advanced Test Plan key |
| `setStandardTestPlanKey(key)` | Set Standard Test Plan key |

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

---

## Developed By

[Vansah](https://vansah.com)
