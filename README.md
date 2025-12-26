# Vansah Cucumber Integration

<p align="center">
  <img src="assets/vansah-logo.png" alt="Vansah Logo" width="200">
</p>

Import Cucumber test results to **Vansah Test Management** using the API import approach (similar to Xray).

## How It Works

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Run Cucumber   │────▶│  Generate JSON  │────▶│  Import to      │
│  Tests (mvn)    │     │  Report         │     │  Vansah API     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

**Unlike hooks-based integrations**, this approach:
- ✅ Runs tests first, imports results after
- ✅ Works with any CI/CD pipeline
- ✅ Single API call to import all results
- ✅ Same workflow as Xray Cucumber integration

## Quick Start

### 1. Configure Environment

Create a `.env` file:

```env
VANSAH_TOKEN=your_vansah_token
VANSAH_URL=https://prod.vansah.com
VANSAH_PROJECT_KEY=SCRUM

# Choose ONE context:
TEST_FOLDER_PATH=SCRUM/Test Repository
# OR
JIRA_ISSUE_KEY=SCRUM-1
```

### 2. Tag Your Scenarios

```gherkin
Feature: Login Feature

  @TC-SCRUM-C1
  Scenario: Valid login
    Given I am on the login page
    When I enter valid credentials
    Then I should see the dashboard

  @TC-SCRUM-C2  
  Scenario: Invalid login
    Given I am on the login page
    When I enter invalid credentials
    Then I should see an error message
```

**Supported tag formats:**
- `@TC-{KEY}` → `@TC-SCRUM-C1`
- `@TESTCASE-{KEY}` → `@TESTCASE-SCRUM-C1`
- `@{KEY}` → `@SCRUM-C1`

### 3. Run Tests & Import Results

**Option A: Using the script**
```bash
./run-tests.sh
```

**Option B: Manual steps**
```bash
# Step 1: Run tests
mvn test

# Step 2: Import to Vansah
cd cli
npm install
node src/cli.js \
  -r ../target/cucumber-reports/cucumber.json \
  -t $VANSAH_TOKEN \
  -p SCRUM \
  -f "SCRUM/Test Repository" \
  --api-url https://prod.vansah.com
```

## CLI Options

```
Usage: vansah-cucumber-import [options]

Options:
  -r, --report <path>       Path to Cucumber JSON report (required)
  -t, --token <token>       Vansah API token (required)
  -p, --project <key>       Jira project key (required)
  -f, --folder <path>       Test folder path in Vansah
  -i, --issue <key>         Jira issue key
  -a, --atp <key>           Advanced Test Plan key
  -s, --stp <key>           Standard Test Plan key
  --sprint <name>           Sprint name
  --release <name>          Release name
  --environment <name>      Environment name
  --step-level              Enable step-level reporting
  --api-url <url>           Vansah API URL (default: https://prod.vansah.com)
  -v, --verbose             Verbose output
```

## API Endpoint

The CLI sends results to:

```
POST /api/v1/cucumber/import
```

**Request payload:**
```json
{
  "projectKey": "SCRUM",
  "testFolderPath": "SCRUM/Test Repository",
  "testRuns": [
    {
      "testCaseKey": "SCRUM-C1",
      "scenarioName": "Valid login",
      "featureName": "Login Feature",
      "status": "PASSED",
      "resultCode": 2,
      "stepCount": 3,
      "duration": 1500000000
    }
  ]
}
```

## CI/CD Integration

### GitHub Actions

```yaml
name: Cucumber Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Run Cucumber Tests
        run: mvn test
      
      - name: Import to Vansah
        run: |
          cd cli
          npm install
          node src/cli.js \
            -r ../target/cucumber-reports/cucumber.json \
            -t ${{ secrets.VANSAH_TOKEN }} \
            -p ${{ vars.VANSAH_PROJECT_KEY }} \
            -f "${{ vars.TEST_FOLDER_PATH }}" \
            --api-url ${{ vars.VANSAH_URL }}
```

### Jenkins

```groovy
pipeline {
    agent any
    
    environment {
        VANSAH_TOKEN = credentials('vansah-token')
    }
    
    stages {
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        
        stage('Import to Vansah') {
            steps {
                dir('cli') {
                    sh 'npm install'
                    sh '''
                        node src/cli.js \
                            -r ../target/cucumber-reports/cucumber.json \
                            -t $VANSAH_TOKEN \
                            -p SCRUM \
                            -f "SCRUM/Test Repository"
                    '''
                }
            }
        }
    }
}
```

## Migration from Xray

| Xray | Vansah |
|------|--------|
| `@XRAY-123` | `@TC-SCRUM-C1` |
| `xray-maven-plugin` | `vansah-cucumber-import` CLI |
| Hooks/Bindings | API Import (same as Xray) |

### Migration steps:

1. Update tags in feature files: `@XRAY-xxx` → `@TC-{key}`
2. Remove Xray dependencies from `pom.xml`
3. Use `vansah-cucumber-import` CLI instead

## Data Residency

| Region | URL |
|--------|-----|
| US (default) | `https://prod.vansah.com` |
| Australia | `https://prodau.vansah.com` |
| Europe | `https://prodeu.vansah.com` |

## Project Structure

```
├── src/
│   └── test/
│       ├── java/
│       │   └── com/testpoint/cucumber/
│       │       ├── runners/
│       │       │   └── CucumberTestRunner.java
│       │       └── steps/
│       │           └── ExampleSteps.java
│       └── resources/
│           └── features/
│               └── example.feature
├── cli/
│   ├── src/
│   │   ├── cli.js          # CLI entry point
│   │   └── processor.js    # Report processor
│   └── package.json
├── pom.xml
├── run-tests.sh            # Convenience script
└── .env                    # Configuration (create this)
```

## License

MIT License - Testpoint Corp
