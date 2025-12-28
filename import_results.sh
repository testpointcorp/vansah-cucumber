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

# Build request body
REQUEST_BODY=$(jq -n \
    --arg projectKey "$VANSAH_PROJECT_KEY" \
    --arg testFolderPath "${TEST_FOLDER_PATH:-}" \
    --arg jiraIssueKey "${JIRA_ISSUE_KEY:-}" \
    --arg sprintName "${SPRINT_NAME:-}" \
    --arg releaseName "${RELEASE_NAME:-}" \
    --arg environmentName "${ENVIRONMENT_NAME:-}" \
    --slurpfile report "$REPORT_PATH" \
    '{
        projectKey: $projectKey,
        testFolderPath: (if $testFolderPath == "" then null else $testFolderPath end),
        jiraIssueKey: (if $jiraIssueKey == "" then null else $jiraIssueKey end),
        sprintName: (if $sprintName == "" then null else $sprintName end),
        releaseName: (if $releaseName == "" then null else $releaseName end),
        environmentName: (if $environmentName == "" then null else $environmentName end),
        cucumberReport: $report[0]
    }')

# Send to Vansah API
echo "📤 Uploading to Vansah..."
echo ""

RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "${VANSAH_URL}/api/v1/cucumber/import" \
    -H "Authorization: Bearer ${VANSAH_TOKEN}" \
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

