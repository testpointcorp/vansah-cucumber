#!/bin/bash
# =============================================================================
# Vansah Cucumber Results Import (JSON Payload)
# =============================================================================
# Usage: ./import_results_json.sh [report_path]
#
# Parses Cucumber JSON report and sends structured testRuns payload to Vansah.
# =============================================================================

set -e

# Default report path
REPORT_PATH="${1:-target/cucumber-reports/cucumber.json}"

# Load .env if exists
if [ -f .env ]; then
    set -a
    source .env
    set +a
fi

# Validate required variables
if [ -z "$VANSAH_TOKEN" ]; then
    echo "❌ Error: VANSAH_TOKEN not set"
    echo "   Set it in .env or export VANSAH_TOKEN=your_token"
    exit 1
fi

if [ -z "$VANSAH_PROJECT_KEY" ]; then
    echo "❌ Error: VANSAH_PROJECT_KEY not set"
    exit 1
fi

# Validate at least one context asset is provided
if [ -z "${TEST_FOLDER_PATH:-}" ] && [ -z "${JIRA_ISSUE_KEY:-}" ] && [ -z "${STANDARD_TEST_PLAN_KEY:-}" ] && [ -z "${ADVANCED_TEST_PLAN_KEY:-}" ]; then
    echo "❌ Error: At least one context asset is required"
    echo "   Set one of: TEST_FOLDER_PATH, JIRA_ISSUE_KEY, STANDARD_TEST_PLAN_KEY, or ADVANCED_TEST_PLAN_KEY"
    exit 1
fi

# Validate advancedTestPlanKey requirements
if [ -n "${ADVANCED_TEST_PLAN_KEY:-}" ] && [ -z "${TEST_FOLDER_PATH:-}" ] && [ -z "${JIRA_ISSUE_KEY:-}" ]; then
    echo "❌ Error: When using ADVANCED_TEST_PLAN_KEY, you must also provide TEST_FOLDER_PATH or JIRA_ISSUE_KEY"
    exit 1
fi

# Set defaults
VANSAH_URL="${VANSAH_URL:-https://prod.vansah.com}"

# Validate report exists
if [ ! -f "$REPORT_PATH" ]; then
    echo "❌ Error: Report not found: $REPORT_PATH"
    echo "   Run 'mvn test' first to generate the report"
    exit 1
fi

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║       VANSAH CUCUMBER RESULTS IMPORT (JSON Payload)         ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""
echo "📄 Report:  $REPORT_PATH"
echo "🔗 API:     $VANSAH_URL"
echo "📁 Project: $VANSAH_PROJECT_KEY"
echo ""

# Process Cucumber report and extract test runs
echo "🔍 Processing Cucumber report..."

# Get current timestamp in ISO8601 format
CURRENT_TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

TEST_RUNS=$(jq --arg timestamp "$CURRENT_TIMESTAMP" '
  # First, create a map of feature names by extracting from top-level elements
  . as $root |
  [.[] | 
    .name as $featureName |
    .elements[]? | 
    # Extract test case key from tags
    (.tags // []) as $tags |
    ([$tags[].name | 
      # Pattern: @TC-XXX-CYYY or @TESTCASE-XXX-CYYY or @XXX-CYYY
      if startswith("@TC-") then .[4:]
      elif startswith("@TESTCASE-") then .[10:]
      elif test("^@[A-Z]+-C[0-9]+$") then .[1:]
      else null end
    ] | map(select(. != null)) | first // null) as $testCaseKey |
    
    # Only process if we have a test case key
    select($testCaseKey != null) |
    
    # Determine status from steps
    (if (.steps // []) | length == 0 then "PASSED"
     elif [.steps[].result.status] | any(. == "failed" or . == "skipped" or . == "undefined" or . == "pending") then "FAILED"
     else "PASSED" end) as $status |
    
    # Result code: 2 = PASSED, 1 = FAILED
    (if $status == "PASSED" then 2 else 1 end) as $resultCode |
    
    # Calculate total duration from steps (in nanoseconds)
    (if (.steps // []) | length > 0 then
      [.steps[].result.duration // 0] | add
    else 0 end) as $duration |
    
    # Extract error message from failed steps (if any)
    (if (.steps // []) | length > 0 then
      [.steps[] | select(.result.status == "failed") | .result.error_message // empty] | first // null
    else null end) as $errorMessage |

    # Build step summary for actualResult
    (if (.steps // []) | length > 0 then
      "Cucumber Test Execution:\n" +
      ([.steps | to_entries[] | "\(.key + 1). \(.value.keyword)\(.value.name) - \(.value.result.status | ascii_upcase)"] | join("\n")) +
      (if $errorMessage then "\n\nError:\n" + $errorMessage else "" end)
    else null end) as $actualResult |
    
    # Build test run object with all required fields
    {
      testCaseKey: $testCaseKey,
      scenarioName: .name,
      status: $status,
      resultCode: $resultCode,
      timestamp: $timestamp
    } + 
    (if $featureName and $featureName != "" then {featureName: $featureName} else {} end) +
    (if $duration > 0 then {duration: $duration} else {} end) +
    (if $actualResult then {actualResult: $actualResult} else {} end)
  ]
' "$REPORT_PATH")

# Count test runs
TEST_COUNT=$(echo "$TEST_RUNS" | jq 'length')
echo "✓ Found $TEST_COUNT test case(s) with valid tags"

if [ "$TEST_COUNT" -eq 0 ]; then
    echo ""
    echo "⚠️  No test cases found with Vansah tags (@TC-XXX-CYY or @XXX-CYY)"
    echo "   Make sure your scenarios have tags like @TD-C95"
    exit 0
fi

# Build request body - only include non-empty optional fields
REQUEST_BODY=$(jq -n \
    --arg projectKey "$VANSAH_PROJECT_KEY" \
    --arg testFolderPath "${TEST_FOLDER_PATH:-}" \
    --arg jiraIssueKey "${JIRA_ISSUE_KEY:-}" \
    --arg standardTestPlanKey "${STANDARD_TEST_PLAN_KEY:-}" \
    --arg advancedTestPlanKey "${ADVANCED_TEST_PLAN_KEY:-}" \
    --arg sprintName "${SPRINT_NAME:-}" \
    --arg releaseName "${RELEASE_NAME:-}" \
    --arg environmentName "${ENVIRONMENT_NAME:-}" \
    --argjson testRuns "$TEST_RUNS" \
    '{
        projectKey: $projectKey,
        testRuns: $testRuns
    } + 
    (if $jiraIssueKey != "" then {jiraIssueKey: $jiraIssueKey} else {} end) +
    (if $testFolderPath != "" then {testFolderPath: $testFolderPath} else {} end) +
    (if $standardTestPlanKey != "" then {standardTestPlanKey: $standardTestPlanKey} else {} end) +
    (if $advancedTestPlanKey != "" then {advancedTestPlanKey: $advancedTestPlanKey} else {} end) +
    (if $sprintName != "" then {sprintName: $sprintName} else {} end) +
    (if $releaseName != "" then {releaseName: $releaseName} else {} end) +
    (if $environmentName != "" then {environmentName: $environmentName} else {} end)')

# Send to Vansah API
echo ""
echo "📤 Uploading to Vansah..."
echo ""

RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "${VANSAH_URL}/api/v1/cucumber/import" \
    -H "Authorization: ${VANSAH_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "$REQUEST_BODY")

# Parse response
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -ge 200 ] && [ "$HTTP_CODE" -lt 300 ]; then
    echo "✅ Success! (HTTP $HTTP_CODE)"
    echo ""
    echo "$BODY" | jq . 2>/dev/null || echo "$BODY"
else
    echo "❌ Failed! (HTTP $HTTP_CODE)"
    echo ""
    echo "$BODY" | jq . 2>/dev/null || echo "$BODY"
    exit 1
fi

echo ""
echo "══════════════════════════════════════════════════════════════════"
