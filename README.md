<div align="center">
  <a href="https://vansah.com"><img src="https://vansah.com/app/logo/vansahjira-logo.svg" /></a><br>
</div>

<p align="center">Import Cucumber test results to <strong>Vansah Test Management for Jira</strong> automatically via API. Seamlessly integrates with any Cucumber-based project regardless of programming language.</p>

<p align="center">
  <a href="https://vansah.com/"><b>Website</b></a> •
  <a href="https://vansah.com/connect-integrations/"><b>More Connect Integrations</b></a>
</p>

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [How It Works](#how-it-works)
- [Step 1 - Get Your Feature File](#step-1---get-your-feature-file)
- [Step 2 - Generate cucumber.json](#step-2---generate-cucumberjson)
- [Step 3 - Configure](#step-3---configure)
- [Step 4 - Import Results](#step-4---import-results)
- [Troubleshooting](#troubleshooting)
  - [PowerShell Script (Windows)](#powershell-script-windows)
  - [Bash Script (macOS / Linux)](#bash-script-macos--linux)
  - [API Errors](#api-errors)

---

## Prerequisites

### 1. Vansah API Token

You need a Vansah API token to authenticate with the API.

1. First generate a [Jira API Token](https://id.atlassian.com/manage-profile/security/api-tokens) from your Atlassian account
2. In your Jira workspace go to **Apps → Vansah → Settings → Vansah API Tokens**
3. Click **Create API Token** and fill in:
   - Label - a name to identify the token
   - Jira Email Address
   - Jira API Token (from step 1)
   - Expiry Date
4. Click **Create** and copy the token immediately - it is shown only once and cannot be retrieved again

> Full guide: [Create Vansah API Token](https://help.vansah.com/en/articles/14003255-create-vansah-api-token)

### 2. Vansah API URL

Your API URL is unique to your Jira workspace region. To find it:

1. Go to **Apps → Vansah → Settings → Vansah API Tokens**
2. The API Connect URL is displayed on that page

Use this URL as `VANSAH_URL` in your configuration. It may differ depending on your workspace's data residency location.

> Full guide: [Vansah API Connect URL](https://help.vansah.com/en/articles/10407923-vansah-api-connect-url)

---

## How It Works

```
Your Cucumber Tests (any language)
        │
        ▼
  cucumber.json  ◄── test result/report file, same format regardless of language
        │
        ▼
  import_results.sh  OR  import_results.ps1
        │
        ▼
  Vansah Test Management (Jira)
```

---

## Step 1 - Get Your Feature File

You have two options: export directly from Vansah (recommended) or tag your existing scenarios manually.

---

### Option A - Export from Vansah UI (Recommended)

If your test cases are already defined in Vansah, you can export them as a ready-to-use `.feature` file with `@TC-KEY` tags pre-populated - no manual tagging needed.

1. Open your Jira project and navigate to **Vansah**
2. Go to the **Test Repository** / **Work Item** / **Test Plan** and select the test cases you want to export
3. Click **Export** → **Test Cases and Test Script** → **Cucumber (.feature)**
4. Vansah reads your BDD-style test cases and downloads a valid `.feature` file

The exported file is immediately usable in any Cucumber framework - just drop it into your `features/` directory.

> For a full walkthrough see the [Cucumber Feature Export](https://help.vansah.com/en/articles/13337729-cucumber-feature-export) article.

---

### Option B - Tag Your Scenarios Manually

If you are writing feature files from scratch, add a Vansah test case tag to each scenario:

```gherkin
Feature: User Authentication

  @TEST-C1
  Scenario: Valid login with correct credentials
    Given I am on the login page
    When I enter valid credentials
    Then I should see the dashboard

  @TEST-C2
  Scenario: Invalid login with wrong password
    Given I am on the login page
    When I enter wrong credentials
    Then I should see an error message
```

### Supported tag format

| Format | Example |
|--------|---------|
| `@{PROJECT}-C{NUMBER}` | `@TEST-C44` |

Scenarios without a Vansah tag are silently skipped during import - your test suite keeps running normally.

---

## Step 2 - Generate cucumber.json

Configure your Cucumber runner to output a JSON report. The command differs per language but the output format is identical.

**JavaScript / TypeScript**
```bash
npx cucumber-js --format json:cucumber.json
```

**Java (Maven)**

Add the JSON formatter to your runner class:
```java
@CucumberOptions(plugin = {"json:target/cucumber-reports/cucumber.json"})
```
Then run your tests:
```bash
mvn test
```

**Python (behave)**
```bash
behave --format json -o cucumber.json
```

**Ruby**
```bash
cucumber --format json --out cucumber.json
```

**.NET (SpecFlow)**

Add the `SpecFlow.Plus.LivingDocPlugin` NuGet package for JSON output, then run your tests:
```bash
dotnet test
```

---

## Step 3 - Configure

Copy `env.example` to `.env` and fill in your values:

```env
VANSAH_TOKEN=your_token_here
VANSAH_PROJECT_KEY=SCRUM
VANSAH_URL=https://prod.vansah.com

# Provide one of the following (required):
TEST_FOLDER_PATH=folderPath/
# JIRA_ISSUE_KEY=SCRUM-1
# STANDARD_TEST_PLAN_KEY=SCRUM-P2

# Advanced Test Plan (optional) - if set, also requires TEST_FOLDER_PATH or JIRA_ISSUE_KEY above
# ADVANCED_TEST_PLAN_KEY=SCRUM-P1

# Optional
# SPRINT_NAME=Sprint 1
# RELEASE_NAME=v1.0.0
# ENVIRONMENT_NAME=UAT
```

**Where to find these values:**
- `VANSAH_TOKEN` - See [Prerequisites → Vansah API Token](#prerequisites)
- `VANSAH_URL` - See [Prerequisites → Vansah API URL](#prerequisites)
- `TEST_FOLDER_PATH` - See [Getting the Folder Path of a Test Folder](https://help.vansah.com/en/articles/15250133-getting-the-folder-path-of-a-test-folder)

---

## Step 4 - Import Results

### Option A - Shell script

**macOS / Linux**

Run with the default report path (`target/cucumber-reports/cucumber.json`):
```bash
./import_results.sh
```

Or specify a custom report path using the `-f` flag:
```bash
./import_results.sh -f path/to/cucumber.json
```

**Windows (PowerShell)**

PowerShell 5.1 is pre-installed on Windows 10/11 - no admin or extra tools required.

Run with the default report path (`cucumber.json`):
```powershell
.\import_results.ps1
```

Or specify a custom report path using the `-f` flag:
```powershell
.\import_results.ps1 -f path\to\cucumber.json
```

> Running into an error? See [Troubleshooting → PowerShell Script (Windows)](#powershell-script-windows).

---

## Troubleshooting

Solutions to common errors across the import scripts.

If your issue is not listed here, [open a GitHub issue](https://github.com/testpointcorp/vansah-cucumber/issues/new) and describe the error, the script/tool you are using, and the full output.

---

## PowerShell Script (Windows)

### Script cannot be loaded - execution policy

**Error**
```
.\import_results.ps1 cannot be loaded because running scripts is disabled on this system.
```

**Fix** - Run once in PowerShell (no admin required):
```powershell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

---

### .env file not being picked up

**Symptom:** Variables set in `.env` are not recognised even though the file exists.

**Fix:** Make sure the `.env` file is in the same directory you are running the script from, and that there are no spaces around the `=` sign:
```env
VANSAH_TOKEN=your_token_here   # correct
VANSAH_TOKEN = your_token_here # incorrect - spaces cause parse failure
```

---

### Script file not found

**Error**
```
The term '.\import_results.ps1' is not recognized
```

**Fix:** Make sure you are in the repository root directory before running the script:
```powershell
cd path\to\vansah-cucumber
.\import_results.ps1 -f path\to\cucumber.json
```

---

### ADVANCED_TEST_PLAN_KEY used without folder or issue

**Error**
```
Error: ADVANCED_TEST_PLAN_KEY requires TEST_FOLDER_PATH or JIRA_ISSUE_KEY
```

**Fix:** Add one of the required context assets alongside `ADVANCED_TEST_PLAN_KEY` in your `.env`:
```env
ADVANCED_TEST_PLAN_KEY=TEST-P1
TEST_FOLDER_PATH=TEST/Test Repository   # add this
```

---

## Bash Script (macOS / Linux)

### Permission denied

**Error**
```
bash: ./import_results.sh: Permission denied
```

**Fix:** Make the script executable:
```bash
chmod +x import_results.sh
```

---

### Report file not found

**Error**
```
❌ Error: Report not found: cucumber.json
```

**Fix:** Run your Cucumber tests first to generate the report (see [Step 2](#step-2---generate-cucumberjson)), then import:
```bash
./import_results.sh -f path/to/cucumber.json
```

---

### VANSAH_TOKEN not set

**Error**
```
❌ Error: VANSAH_TOKEN not set
```

**Fix:** Either add it to your `.env` file or export it in your shell:
```bash
export VANSAH_TOKEN=your_token_here
```

---

### ADVANCED_TEST_PLAN_KEY used without folder or issue

**Error**
```
❌ Error: When using ADVANCED_TEST_PLAN_KEY, you must also provide TEST_FOLDER_PATH or JIRA_ISSUE_KEY
```

**Fix:** Add one of the required context assets alongside `ADVANCED_TEST_PLAN_KEY` in your `.env`:
```env
ADVANCED_TEST_PLAN_KEY=TEST-P1
TEST_FOLDER_PATH=TEST/Test Repository   # add this
```

---

## API Errors

### HTTP 401 - Unauthorized

**Cause:** The `VANSAH_TOKEN` is invalid, expired, or missing.

**Fix:**
- Confirm the token is copied correctly with no extra spaces
- Check the token expiry date in **Apps → Vansah → Settings → Vansah API Tokens**
- Generate a new token if expired - see [Create Vansah API Token](https://help.vansah.com/en/articles/14003255-create-vansah-api-token)

---

### HTTP 403 - Forbidden

**Cause:** The token does not have permission for this project.

**Fix:** Ensure the Jira account used to generate the token has access to the project specified in `VANSAH_PROJECT_KEY`.

---

### HTTP 404 - Not Found

**Cause:** Wrong API URL or project key.

**Fix:**
- Verify `VANSAH_URL` matches your workspace's API Connect URL - see [Vansah API Connect URL](https://help.vansah.com/en/articles/10407923-vansah-api-connect-url)
- Verify `VANSAH_PROJECT_KEY` matches your Jira project key exactly (case-sensitive)

---

### HTTP 500 - Internal Server Error

**Cause:** An unexpected error on the Vansah server side.

**Fix:** Retry after a short wait. If the error persists, contact [Vansah support](https://vansah.com/) with the response body.

---

## Still stuck?

If your issue is not covered above, [open a GitHub issue](https://github.com/testpointcorp/vansah-cucumber/issues/new) and include:

- The script you are using (`import_results.sh` or `import_results.ps1`)
- The full error message or output
- Your OS and shell

---

<p align="center">Developed by <a href="https://vansah.com"><strong>Vansah</strong></a></p>


<!-- Security scan triggered at 2026-08-31 16:42:03 -->