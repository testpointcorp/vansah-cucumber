# Vansah Cucumber Integration

<p align="center">
  <img src="assets/vansah-logo.png" alt="Vansah Logo" width="200">
</p>

Import Cucumber test results to **Vansah Test Management** - simple API import approach (like Xray).

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

## API Endpoint

```
POST /api/v1/cucumber/import
```

The script sends the Cucumber JSON report directly to Vansah's API.

## Data Residency

| Region | URL |
|--------|-----|
| US (default) | `https://prod.vansah.com` |
| Australia | `https://prodau.vansah.com` |
| Europe | `https://prodeu.vansah.com` |

## Project Structure

```
├── src/test/
│   ├── java/.../
│   │   ├── runners/CucumberTestRunner.java
│   │   └── steps/ExampleSteps.java
│   └── resources/features/
│       └── example.feature
├── pom.xml
├── import_results.sh    ← Import script
├── env.example          ← Config template
└── README.md
```

## Migration from Xray

| Xray | Vansah |
|------|--------|
| `@XRAY-123` | `@TC-SCRUM-C1` |
| `import_results_cloud.sh` | `import_results.sh` |

## Requirements

- Java 11+
- Maven
- `jq` (for JSON processing)
- `curl`

## License

MIT License - Testpoint Corp
