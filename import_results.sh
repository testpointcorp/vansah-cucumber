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

# Start building curl arguments
CURL_ARGS=(
    -s -w "\n%{http_code}"
    -X POST "${VANSAH_URL}/api/v1/cucumber/import"
    -H "Authorization: ${VANSAH_TOKEN}"
    -F "Testformat=Cucumber_json"
    -F "Testpath=@${REPORT_PATH};type=application/json"
    -F "projectKey=${VANSAH_PROJECT_KEY}"
)

# Add optional fields if set
[ -n "${TEST_FOLDER_PATH:-}" ] && CURL_ARGS+=(-F "testFolderPath=${TEST_FOLDER_PATH}")
[ -n "${JIRA_ISSUE_KEY:-}" ] && CURL_ARGS+=(-F "jiraIssueKey=${JIRA_ISSUE_KEY}")
[ -n "${SPRINT_NAME:-}" ] && CURL_ARGS+=(-F "sprintName=${SPRINT_NAME}")
[ -n "${RELEASE_NAME:-}" ] && CURL_ARGS+=(-F "releaseName=${RELEASE_NAME}")
[ -n "${ENVIRONMENT_NAME:-}" ] && CURL_ARGS+=(-F "environmentName=${ENVIRONMENT_NAME}")

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
