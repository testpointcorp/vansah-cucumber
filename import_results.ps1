<#
=============================================================================
 Vansah Cucumber Results Import (Form-Data) - PowerShell
=============================================================================
 Usage:
   .\import_results.ps1 [report_path]

 Default report path:
   target/cucumber-reports/cucumber.json

 Uploads Cucumber JSON report to Vansah Test Management using multipart form-data.
=============================================================================
#>

param (
    [string]$ReportPath = "target/cucumber-reports/cucumber.json"
)

$ErrorActionPreference = "Stop"

# --------------------------------------------------
# Load .env if exists
# --------------------------------------------------
if (Test-Path ".env") {
    Get-Content ".env" |
        Where-Object { $_ -and $_ -notmatch '^\s*#' } |
        ForEach-Object {
            $key, $value = $_ -split '=', 2
            if ($key -and $value) {
                [System.Environment]::SetEnvironmentVariable($key.Trim(), $value.Trim())
            }
        }
}

# --------------------------------------------------
# Validate required variables
# --------------------------------------------------
if (-not $env:VANSAH_TOKEN) {
    Write-Host "Error: VANSAH_TOKEN is not set" -ForegroundColor Red
    Write-Host "Set it in .env or run:"
    Write-Host "  `$env:VANSAH_TOKEN = 'your_token_here'"
    exit 1
}

# Defaults
$VANSAH_URL = if ($env:VANSAH_URL) {
    $env:VANSAH_URL
} else {
    Write-Host "Please provide VANSAH_URL in .env or environment variable." -ForegroundColor Red
}

# --------------------------------------------------
# Validate report exists
# --------------------------------------------------
if (-not (Test-Path $ReportPath)) {
    Write-Host "Error: Report not found: $ReportPath" -ForegroundColor Red
    Write-Host "Run 'mvn test' to generate the report"
    exit 1
}

Write-Host "---------------------------------------------------------------"
Write-Host "VANSAH - Cucumber Results Import"
Write-Host "---------------------------------------------------------------"
Write-Host "Report Path : $ReportPath"
Write-Host "API URL     : $VANSAH_URL"
Write-Host ""

Write-Host "Uploading Cucumber report to Vansah..."
Write-Host ""

# --------------------------------------------------
# Build multipart form-data
# --------------------------------------------------
Add-Type -AssemblyName System.Net.Http

$handler = New-Object System.Net.Http.HttpClientHandler
$client  = New-Object System.Net.Http.HttpClient($handler)

$client.DefaultRequestHeaders.Add("Authorization", $env:VANSAH_TOKEN)

$form = New-Object System.Net.Http.MultipartFormDataContent

# Required fields
$form.Add(
    (New-Object System.Net.Http.StringContent("Cucumber_json")),
    "Testformat"
)

$fileStream = [System.IO.File]::OpenRead($ReportPath)
$fileContent = New-Object System.Net.Http.StreamContent($fileStream)
$fileContent.Headers.ContentType =
    [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("application/json")

$form.Add(
    $fileContent,
    "Testpath",
    [System.IO.Path]::GetFileName($ReportPath)
)

# Optional fields
if ($env:TEST_FOLDER_PATH) {
    $form.Add((New-Object System.Net.Http.StringContent($env:TEST_FOLDER_PATH)), "testFolderPath")
}
if ($env:JIRA_ISSUE_KEY) {
    $form.Add((New-Object System.Net.Http.StringContent($env:JIRA_ISSUE_KEY)), "jiraIssueKey")
}
if ($env:SPRINT_NAME) {
    $form.Add((New-Object System.Net.Http.StringContent($env:SPRINT_NAME)), "sprintName")
}
if ($env:RELEASE_NAME) {
    $form.Add((New-Object System.Net.Http.StringContent($env:RELEASE_NAME)), "releaseName")
}
if ($env:ENVIRONMENT_NAME) {
    $form.Add((New-Object System.Net.Http.StringContent($env:ENVIRONMENT_NAME)), "environmentName")
}

# --------------------------------------------------
# Execute request
# --------------------------------------------------
$response       = $client.PostAsync("$VANSAH_URL/api/v1/cucumber/import", $form).Result
$responseBody   = $response.Content.ReadAsStringAsync().Result
$statusCode     = [int]$response.StatusCode

# Cleanup
$fileStream.Close()
$client.Dispose()

# --------------------------------------------------
# Handle response (Human-readable)
# --------------------------------------------------
if ($statusCode -ge 200 -and $statusCode -lt 300) {

    $result = $null
    try {
        $result = $responseBody | ConvertFrom-Json
    } catch {
        Write-Host "Upload completed, but response could not be parsed."
        Write-Host $responseBody
        exit 0
    }

    $passed  = ($result.testRuns | Where-Object { $_.status -eq "PASSED" }).Count
    $failed  = ($result.testRuns | Where-Object { $_.status -eq "FAILED" }).Count
    $skipped = ($result.testRuns | Where-Object { $_.status -eq "SKIPPED" }).Count

    Write-Host "Upload completed successfully (HTTP $statusCode)" -ForegroundColor Green
    Write-Host ""
    Write-Host "Summary:"
    Write-Host "  Total Imported : $($result.imported)"
    Write-Host "  Passed         : $passed"
    Write-Host "  Failed         : $failed"
    Write-Host "  Skipped        : $skipped"
    Write-Host "  Warnings       : $($result.warnings.Count)"
    Write-Host ""
    Write-Host "Test Run Results:"

    foreach ($run in $result.testRuns) {
        Write-Host "  $($run.testCaseKey) -> $($run.status)"
    }

    if ($failed -gt 0) {
        Write-Host ""
        Write-Host "One or more test cases failed." -ForegroundColor Yellow
    }
}
else {
    Write-Host "Upload failed (HTTP $statusCode)" -ForegroundColor Red
    Write-Host ""

    try {
        $responseBody | ConvertFrom-Json | ConvertTo-Json -Depth 10
    }
    catch {
        Write-Host $responseBody
    }
    exit 1
}

Write-Host ""
Write-Host "---------------------------------------------------------------"
