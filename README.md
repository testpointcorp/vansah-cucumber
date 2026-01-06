<p align="center">
  <img src="https://camo.githubusercontent.com/e61a5aa89c4c62feb8067fffdfa47f99ec134e6d938822501728198a3ffbdee9/68747470733a2f2f76616e7361682e636f6d2f6170702f6c6f676f2f76616e7361686a6972612d6c6f676f2e737667" alt="Vansah Logo" width="200">
</p>

Import Cucumber test results to **Vansah Test Management** via API.

## Overview

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   mvn test      │────▶│  cucumber.json  │────▶│  Vansah API     │
│                 │     │                 │     │  (curl import)  │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

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

### 1. Add Scenario parameter to your step

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

### 2. Import results (attachments uploaded automatically)

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

## CI/CD Integration

### GitHub Actions

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

## API Endpoint

```
POST /api/v1/cucumber/import
```

The script sends the Cucumber JSON report directly to Vansah's API.

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
├── pom.xml
├── import_results.sh      ← Import script
├── env.example            ← Config template
└── README.md
```

## Requirements

- Java 11+
- Maven
- `jq` (for JSON processing)
- `curl`

## License

MIT License - Testpoint Corp
