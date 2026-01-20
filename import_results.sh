#!/bin/bash
# =============================================================================
# Vansah Cucumber Results Import (Form-Data)
# =============================================================================
# Usage: ./import_results.sh [report_path]
#
# Uploads Cucumber JSON report to Vansah Test Management via multipart form-data.
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
    echo "   Set it in .env or export VANSAH_PROJECT_KEY=your_project_key"
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
echo "║        VANSAH CUCUMBER RESULTS IMPORT (Form-Data)           ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""
echo "📄 Report:  $REPORT_PATH"
echo "🔗 API:     $VANSAH_URL"
echo "📁 Project: $VANSAH_PROJECT_KEY"
echo ""

# Build curl command with form-data
echo "📤 Uploading Cucumber report to Vansah..."
echo ""

# Start building curl arguments - Required fields
CURL_ARGS=(
    -s -w "\n%{http_code}"
    -X POST "${VANSAH_URL}/api/v1/cucumber/import"
    -H "Authorization: ${VANSAH_TOKEN}"
    -F "Testformat=Cucumber_json"
    -F "Testpath=@${REPORT_PATH};type=application/json"
    -F "projectKey=${VANSAH_PROJECT_KEY}"
)

# Add context assets (at least one required)
[ -n "${JIRA_ISSUE_KEY:-}" ] && CURL_ARGS+=(-F "jiraIssueKey=${JIRA_ISSUE_KEY}")
[ -n "${TEST_FOLDER_PATH:-}" ] && CURL_ARGS+=(-F "testFolderPath=${TEST_FOLDER_PATH}")
[ -n "${STANDARD_TEST_PLAN_KEY:-}" ] && CURL_ARGS+=(-F "standardTestPlanKey=${STANDARD_TEST_PLAN_KEY}")
[ -n "${ADVANCED_TEST_PLAN_KEY:-}" ] && CURL_ARGS+=(-F "advancedTestPlanKey=${ADVANCED_TEST_PLAN_KEY}")

# Add optional fields if set
[ -n "${SPRINT_NAME:-}" ] && CURL_ARGS+=(-F "sprintName=${SPRINT_NAME}")
[ -n "${RELEASE_NAME:-}" ] && CURL_ARGS+=(-F "releaseName=${RELEASE_NAME}")
[ -n "${ENVIRONMENT_NAME:-}" ] && CURL_ARGS+=(-F "environmentName=${ENVIRONMENT_NAME}")
[ -n "${STEP_LEVEL_REPORTING:-}" ] && CURL_ARGS+=(-F "stepLevelReporting=${STEP_LEVEL_REPORTING}")

# Execute request
RESPONSE=$(curl "${CURL_ARGS[@]}")

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
