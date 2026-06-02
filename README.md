<div align="center">
  <a href="https://vansah.com"><img src="https://vansah.com/app/logo/vansahjira-logo.svg" /></a><br>
</div>

<p align="center">Import Cucumber test results to <strong>Vansah Test Management for Jira</strong> automatically via API. Seamlessly integrates with Maven, Selenium, and any Java-based Cucumber project.</p>

<p align="center">
  <a href="https://vansah.com/"><b>Website</b></a> •
  <a href="https://vansah.com/connect-integrations/"><b>More Connect Integrations</b></a>
</p>

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Tag Formats](#tag-formats)
- [Screenshots & Attachments](#screenshots--attachments)
- [CLI Options](#cli-options)
- [CI/CD Integration](#cicd-integration)
  - [GitHub Actions](#github-actions)
  - [Jenkins](#jenkins)
  - [Bitbucket Pipelines](#bitbucket-pipelines)
- [Project Structure](#project-structure)
- [API Endpoint](#api-endpoint)

## Features

- Automatically import Cucumber JSON test results to Vansah Test Management for Jira.
- Map test cases using tags like `@TC-{KEY}`, `@TESTCASE-{KEY}`, or `@{KEY}`.
- Support for step-level reporting with detailed test logs.
- Automatic screenshot and attachment uploads embedded in Cucumber JSON.
- Integration with CI/CD pipelines (GitHub Actions, Jenkins, Bitbucket Pipelines).
- Flexible configuration via environment variables or CLI options.

## Prerequisites

- Make sure that [`Vansah`](https://marketplace.atlassian.com/apps/1224250/vansah-test-management-for-jira?tab=overview&hosting=cloud) is installed in your Jira workspace.
- You need to generate a Vansah [`connect`](https://help.vansah.com/en/articles/9824979-generate-a-vansah-api-token-from-jira) token to authenticate with Vansah APIs.
- Java JDK 11 or newer.
- Maven installed.
- `jq` (for JSON processing) and `curl` for the shell script.

## Quick Start

### 1. Configure

```bash
cp env.example .env
# Edit .env with your Vansah credentials
```

### 2. Tag Your Scenarios

```gherkin
Feature: Login Feature

  @TC-SCRUM-C1
  Scenario: Valid login
    Given I am on the login page
    When I enter valid credentials
    Then I should see the dashboard
```

### 3. Run Tests & Import

```bash
# Run tests
mvn test

# Import results to Vansah
./import_results.sh
```

## Configuration

Create a `.env` file (copy from `env.example`):

```env
# Required
VANSAH_TOKEN=your_token_here
VANSAH_PROJECT_KEY=SCRUM
VANSAH_URL=https://prod.vansah.com

# Context (at least one)
TEST_FOLDER_PATH=SCRUM/Test Repository
# JIRA_ISSUE_KEY=SCRUM-1

# Optional
# SPRINT_NAME=Sprint 1
# RELEASE_NAME=v1.0.0
# ENVIRONMENT_NAME=UAT
```

## Tag Formats

| Format | Example |
|--------|---------|
| `@TC-{KEY}` | `@TC-SCRUM-C1` |
| `@TESTCASE-{KEY}` | `@TESTCASE-SCRUM-C1` |
| `@{KEY}` | `@SCRUM-C1` |

## Screenshots & Attachments

To attach screenshots to your Vansah test runs, embed them in your step definitions using `scenario.attach()`:

### Add Scenario parameter to your step

```java
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

@Then("I verify the dashboard")
public void iVerifyTheDashboard(Scenario scenario) {
    // Your test logic...
    
    // Capture and attach screenshot
    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    scenario.attach(screenshot, "image/png", "dashboard.png");
}
```

### Import results (attachments uploaded automatically)

```bash
npx vansah-cucumber-import \
  -r target/cucumber-reports/cucumber.json \
  -t $VANSAH_TOKEN \
  -p $PROJECT_KEY \
  -f "Test Folder"
```

Attachments are detected and uploaded automatically if present in the JSON report.

### Supported attachment types

| MIME Type | Use Case |
|-----------|----------|
| `image/png` | Screenshots |
| `image/jpeg` | Screenshots |
| `text/plain` | Logs, text output |
| `text/html` | Page source |
| `application/json` | API responses |

## CLI Options

```bash
npx vansah-cucumber-import [options]

Options:
  -r, --report <path>      Path to Cucumber JSON report (required)
  -t, --token <token>      Vansah API token (required)
  -p, --project <key>      Jira project key (required)
  -f, --folder <path>      Test folder path in Vansah
  -i, --issue <key>        Jira issue key
  -a, --atp <key>          Advanced Test Plan key
  -s, --stp <key>          Standard Test Plan key
  --sprint <name>          Sprint name
  --release <name>         Release name
  --environment <name>     Environment name
  --step-level             Enable step-level reporting
  --api-url <url>          Vansah API URL (default: https://prodau.vansah.com)
  -v, --verbose            Verbose output
```

**Note:** Attachments (screenshots, logs) embedded in the Cucumber JSON are automatically detected and uploaded.

## CI/CD Integration

> **Ready-to-use example files** are available in the [`ci-examples/`](ci-examples/) directory:
> - [GitHub Actions workflow](ci-examples/github-actions-cucumber.yml) — copy to `.github/workflows/`
> - [Jenkinsfile](ci-examples/Jenkinsfile) — copy to repository root
> - [Bitbucket Pipelines](ci-examples/bitbucket-pipelines.yml) — copy to repository root
>
> These files include detailed comments on required secrets/variables and won't auto-run from their current location.

### GitHub Actions

Copy [`ci-examples/github-actions-cucumber.yml`](ci-examples/github-actions-cucumber.yml) to `.github/workflows/cucumber-tests.yml`, or use the snippet below:

```yaml
name: Cucumber Tests

on: [push]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Run Tests
        run: mvn test
      
      - name: Import to Vansah
        env:
          VANSAH_TOKEN: ${{ secrets.VANSAH_TOKEN }}
          VANSAH_PROJECT_KEY: ${{ vars.VANSAH_PROJECT_KEY }}
          VANSAH_URL: ${{ vars.VANSAH_URL }}
          TEST_FOLDER_PATH: ${{ vars.TEST_FOLDER_PATH }}
        run: ./import_results.sh
```

### Jenkins

Copy [`ci-examples/Jenkinsfile`](ci-examples/Jenkinsfile) to your repository root, or use the snippet below:

```groovy
pipeline {
    agent any
    environment {
        VANSAH_TOKEN = credentials('vansah-token')
        VANSAH_PROJECT_KEY = 'SCRUM'
        VANSAH_URL = 'https://prod.vansah.com'
        TEST_FOLDER_PATH = 'SCRUM/Test Repository'
    }
    stages {
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Import') {
            steps {
                sh './import_results.sh'
            }
        }
    }
}
```

### Bitbucket Pipelines

Copy [`ci-examples/bitbucket-pipelines.yml`](ci-examples/bitbucket-pipelines.yml) to your repository root, or use the snippet below:

```yaml
image: maven:3.8-openjdk-11

pipelines:
  default:
    - step:
        name: Run Tests
        caches:
          - maven
        script:
          - mvn test
        artifacts:
          - target/cucumber-reports/**

    - step:
        name: Import to Vansah
        script:
          - apt-get update && apt-get install -y jq
          - ./import_results.sh
```

Set these repository variables in Bitbucket:
- `VANSAH_TOKEN` (secured)
- `VANSAH_PROJECT_KEY`
- `VANSAH_URL`
- `TEST_FOLDER_PATH`

## Project Structure

```
├── src/test/
│   ├── java/.../
│   │   ├── runners/CucumberTestRunner.java
│   │   └── steps/ExampleSteps.java
│   └── resources/features/
│       └── example.feature
├── cli/                   ← Node.js CLI tool
│   └── src/
│       ├── cli.js
│       └── processor.js
├── ci-examples/           ← CI/CD pipeline templates
│   ├── github-actions-cucumber.yml
│   ├── Jenkinsfile
│   └── bitbucket-pipelines.yml
├── pom.xml
├── import_results.sh      ← Import script
├── env.example            ← Config template
└── README.md
```

## API Endpoint

```
POST /api/v2/cucumber/import
```

The script sends the Cucumber JSON report directly to Vansah's API.

## Developed By

[Vansah](https://vansah.com/)
