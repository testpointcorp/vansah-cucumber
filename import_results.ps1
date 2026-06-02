# =============================================================================
# Vansah Cucumber Results Import (Form-Data) - Windows PowerShell
# =============================================================================
# Usage: .\import_results.ps1 [-f report_path]
#
# Compatible with Windows PowerShell 5.1+ (pre-installed on Windows 10/11).
# No admin or elevated privileges required.
#
# If you see a script execution error, run this command once in PowerShell:
#   Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
# =============================================================================

param(
    [Alias("f")]
    [string]$ReportPath = "cucumber.json"
)

# Load .env file if it exists
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+?)\s*=\s*(.*)\s*$') {
            $key   = $matches[1].Trim()
            $value = $matches[2].Trim().Trim('"').Trim("'")
            if (-not [System.Environment]::GetEnvironmentVariable($key)) {
                [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
            }
        }
    }
}

# Read variables
$Token               = $env:VANSAH_TOKEN
$ProjectKey          = $env:VANSAH_PROJECT_KEY
$VansahUrl           = if ($env:VANSAH_URL) { $env:VANSAH_URL } else { "https://prod.vansah.com" }
$TestFolderPath      = $env:TEST_FOLDER_PATH
$JiraIssueKey        = $env:JIRA_ISSUE_KEY
$StandardTestPlanKey = $env:STANDARD_TEST_PLAN_KEY
$AdvancedTestPlanKey = $env:ADVANCED_TEST_PLAN_KEY
$SprintName          = $env:SPRINT_NAME
$ReleaseName         = $env:RELEASE_NAME
$EnvironmentName     = $env:ENVIRONMENT_NAME

# Validate required variables
if (-not $Token) {
    Write-Host "Error: VANSAH_TOKEN not set" -ForegroundColor Red
    Write-Host "  Set it in .env or run: `$env:VANSAH_TOKEN='your_token'" -ForegroundColor Yellow
    exit 1
}

if (-not $ProjectKey) {
    Write-Host "Error: VANSAH_PROJECT_KEY not set" -ForegroundColor Red
    Write-Host "  Set it in .env or run: `$env:VANSAH_PROJECT_KEY='your_project_key'" -ForegroundColor Yellow
    exit 1
}

if (-not $TestFolderPath -and -not $JiraIssueKey -and -not $StandardTestPlanKey -and -not $AdvancedTestPlanKey) {
    Write-Host "Error: At least one context asset is required" -ForegroundColor Red
    Write-Host "  Set one of: TEST_FOLDER_PATH, JIRA_ISSUE_KEY, STANDARD_TEST_PLAN_KEY, or ADVANCED_TEST_PLAN_KEY" -ForegroundColor Yellow
    exit 1
}

if ($AdvancedTestPlanKey -and -not $TestFolderPath -and -not $JiraIssueKey) {
    Write-Host "Error: ADVANCED_TEST_PLAN_KEY requires TEST_FOLDER_PATH or JIRA_ISSUE_KEY" -ForegroundColor Red
    exit 1
}

# Validate report file exists
if (-not (Test-Path $ReportPath)) {
    Write-Host "Error: Report not found: $ReportPath" -ForegroundColor Red
    Write-Host "  Run your Cucumber tests first to generate the report" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "     VANSAH CUCUMBER RESULTS IMPORT (Form-Data)" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Report:  $ReportPath"
Write-Host "API:     $VansahUrl"
Write-Host "Project: $ProjectKey"
Write-Host ""
Write-Host "Uploading Cucumber report to Vansah..." -ForegroundColor Yellow
Write-Host ""

# Build multipart form using .NET HttpClient (compatible with PowerShell 5.1+)
Add-Type -AssemblyName System.Net.Http

$client  = New-Object System.Net.Http.HttpClient
$client.DefaultRequestHeaders.Add("Authorization", $Token)

$form = New-Object System.Net.Http.MultipartFormDataContent

# Required fields
$form.Add((New-Object System.Net.Http.StringContent("Cucumber_json")), "Testformat")
$form.Add((New-Object System.Net.Http.StringContent($ProjectKey)),     "projectKey")

# Attach the report file
$fileBytes   = [System.IO.File]::ReadAllBytes((Resolve-Path $ReportPath))
$fileContent = New-Object System.Net.Http.ByteArrayContent(, $fileBytes)
$fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("application/json")
$form.Add($fileContent, "Testpath", [System.IO.Path]::GetFileName($ReportPath))

# Context assets
if ($TestFolderPath)      { $form.Add((New-Object System.Net.Http.StringContent($TestFolderPath)),      "testFolderPath") }
if ($JiraIssueKey)        { $form.Add((New-Object System.Net.Http.StringContent($JiraIssueKey)),        "jiraIssueKey") }
if ($StandardTestPlanKey) { $form.Add((New-Object System.Net.Http.StringContent($StandardTestPlanKey)), "standardTestPlanKey") }
if ($AdvancedTestPlanKey) { $form.Add((New-Object System.Net.Http.StringContent($AdvancedTestPlanKey)), "advancedTestPlanKey") }

# Optional fields
if ($SprintName)         { $form.Add((New-Object System.Net.Http.StringContent($SprintName)),         "sprintName") }
if ($ReleaseName)        { $form.Add((New-Object System.Net.Http.StringContent($ReleaseName)),        "releaseName") }
if ($EnvironmentName)    { $form.Add((New-Object System.Net.Http.StringContent($EnvironmentName)),    "environmentName") }

# Send request
try {
    $response   = $client.PostAsync("$VansahUrl/api/v2/cucumber/import", $form).Result
    $body       = $response.Content.ReadAsStringAsync().Result
    $statusCode = [int]$response.StatusCode

    if ($statusCode -ge 200 -and $statusCode -lt 300) {
        Write-Host "Success! (HTTP $statusCode)" -ForegroundColor Green
        Write-Host ""
        try {
            $parsed = $body | ConvertFrom-Json
            Write-Host ($parsed | ConvertTo-Json -Depth 10)
        } catch {
            Write-Host $body
        }
    } else {
        Write-Host "Failed! (HTTP $statusCode)" -ForegroundColor Red
        Write-Host ""
        try {
            $parsed = $body | ConvertFrom-Json
            Write-Host ($parsed | ConvertTo-Json -Depth 10)
        } catch {
            Write-Host $body
        }
        exit 1
    }
} catch {
    Write-Host "Request failed: $_" -ForegroundColor Red
    exit 1
} finally {
    $client.Dispose()
}

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
