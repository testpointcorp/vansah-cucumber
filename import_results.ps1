<#
=============================================================================
 Vansah Cucumber Results Import (Windows / PowerShell)
=============================================================================
 Usage:
   powershell -ExecutionPolicy Bypass -File import_results.ps1 [report_path]

 Imports Cucumber JSON results to Vansah Test Management.
=============================================================================
#>

param (
    [string]$ReportPath = "target/cucumber-reports/cucumber.json"
)

$ErrorActionPreference = "Stop"

# Load .env if exists
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^\s*([^#][^=]+)=(.*)$") {
            $name = $matches[1]
            $value = $matches[2]
            [Environment]::SetEnvironmentVariable($name, $value)
        }
    }
}

# Validate required variables
if (-not $env:VANSAH_TOKEN) {
    Write-Host "ERROR: VANSAH_TOKEN not set"
    exit 1
}

if (-not $env:VANSAH_PROJECT_KEY) {
    Write-Host "ERROR: VANSAH_PROJECT_KEY not set"
    exit 1
}

# Defaults
$VANSAH_URL = if ($env:VANSAH_URL) { $env:VANSAH_URL } else { "https://prod.vansah.com" }

# Validate report exists
if (-not (Test-Path $ReportPath)) {
    Write-Host "ERROR: Report not found: $ReportPath"
    exit 1
}

Write-Host "=============================================="
Write-Host "   VANSAH CUCUMBER RESULTS IMPORT"
Write-Host "=============================================="
Write-Host "Report : $ReportPath"
Write-Host "API    : $VANSAH_URL"
Write-Host "Project: $env:VANSAH_PROJECT_KEY"
Write-Host ""

# Read cucumber report
$CucumberReport = Get-Content $ReportPath -Raw | ConvertFrom-Json

# Build request body
$RequestBody = @{
    projectKey      = $env:VANSAH_PROJECT_KEY
    testFolderPath  = $env:TEST_FOLDER_PATH
    jiraIssueKey    = $env:JIRA_ISSUE_KEY
    sprintName      = $env:SPRINT_NAME
    releaseName     = $env:RELEASE_NAME
    environmentName = $env:ENVIRONMENT_NAME
    cucumberReport  = $CucumberReport
} | ConvertTo-Json -Depth 20

Write-Host "Uploading to Vansah..."
Write-Host ""

try {
    $Response = Invoke-RestMethod `
        -Uri "$VANSAH_URL/api/v1/cucumber/import" `
        -Method POST `
        -Headers @{
            Authorization = "Bearer $($env:VANSAH_TOKEN)"
            "Content-Type" = "application/json"
        } `
        -Body $RequestBody

    Write-Host "SUCCESS"
    $Response | ConvertTo-Json -Depth 10
}
catch {
    Write-Host "FAILED"

    if ($_.Exception.Response) {
        $response = $_.Exception.Response
        Write-Host ""
        Write-Host "HTTP Status Code:" $response.StatusCode.value__
        Write-Host "Status Description:" $response.StatusDescription
        Write-Host ""

        $stream = $response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $body = $reader.ReadToEnd()

        Write-Host "Response Body:"
        Write-Host "-----------------------------"
        Write-Host $body
        Write-Host "-----------------------------"
    }
    else {
        Write-Host "Error Message:"
        Write-Host $_.Exception.Message
    }

    exit 1
}
