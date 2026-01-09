#!/bin/bash
# =============================================================================
# Vansah Cucumber Results Import
# =============================================================================
# Usage: ./import_results.sh [report_path]
#
# Imports Cucumber JSON results to Vansah Test Management.
# =============================================================================

set -e

# Default report path
REPORT_PATH="${1:-target/cucumber-reports/cucumber.json}"

# Load .env if exists
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
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

# Set defaults
VANSAH_URL="${VANSAH_URL:-https://prod.vansah.com}"

# Validate report exists
if [ ! -f "$REPORT_PATH" ]; then
    echo "❌ Error: Report not found: $REPORT_PATH"
    echo "   Run 'mvn test' first to generate the report"
    exit 1
fi

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║           VANSAH CUCUMBER RESULTS IMPORT                     ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""
echo "📄 Report:  $REPORT_PATH"
echo "🔗 API:     $VANSAH_URL"
echo "📁 Project: $VANSAH_PROJECT_KEY"
echo ""

# Process Cucumber report and extract test runs
echo "🔍 Processing Cucumber report..."

TEST_RUNS=$(jq '
  [.[] | .elements[]? | 
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
     elif [.steps[].result.status] | any(. == "failed" or . == "skipped") then "FAILED"
     else "PASSED" end) as $status |
    
    # Result code: 2 = PASSED, 1 = FAILED
    (if $status == "PASSED" then 2 else 1 end) as $resultCode |
    
    # Build step summary for actualResult
    (if (.steps // []) | length > 0 then
      "Cucumber Test Execution:\n" + ([.steps | to_entries[] | "\(.key + 1). \(.value.keyword)\(.value.name) - \(.value.result.status | ascii_upcase)"] | join("\n"))
    else null end) as $actualResult |
    
    # Build test run object
    {
      testCaseKey: $testCaseKey,
      scenarioName: .name,
      status: $status,
      resultCode: $resultCode
    } + 
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
    --arg sprintName "${SPRINT_NAME:-}" \
    --arg releaseName "${RELEASE_NAME:-}" \
    --arg environmentName "${ENVIRONMENT_NAME:-}" \
    --argjson testRuns "$TEST_RUNS" \
    '{
        projectKey: $projectKey,
        testRuns: $testRuns
    } + 
    (if $testFolderPath != "" then {testFolderPath: $testFolderPath} else {} end) +
    (if $jiraIssueKey != "" then {jiraIssueKey: $jiraIssueKey} else {} end) +
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
