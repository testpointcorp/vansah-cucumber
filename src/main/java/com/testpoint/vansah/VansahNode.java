package com.testpoint.vansah;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * VansahNode class for integrating with Vansah Test Management API.
 * This class provides methods to create test runs, log test results, and manage test assets.
 */
public class VansahNode {
    private String vansahToken;
    private String vansahApiUrl = "https://api.vansah.net";
    private String testFolderPath;
    private String jiraIssueKey;
    private String sprintName;
    private String releaseName;
    private String environmentName;
    private String advancedTestPlanKey;
    private String standardTestPlanKey;
    private String currentTestRunId;
    private Gson gson = new Gson();

    /**
     * Constructor that initializes VansahNode with token and API URL from environment variables.
     */
    public VansahNode() {
        this.vansahToken = System.getenv("VANSAH_TOKEN");
        String apiUrl = System.getenv("VANSAH_API_URL");
        if (apiUrl != null && !apiUrl.isEmpty()) {
            this.vansahApiUrl = apiUrl;
        }
    }

    /**
     * Sets the Vansah authentication token.
     * @param token The Vansah Connect token
     */
    public void setVansahToken(String token) {
        this.vansahToken = token;
    }

    /**
     * Sets the Vansah API URL.
     * @param apiUrl The base URL for Vansah API
     */
    public void setVansahApiUrl(String apiUrl) {
        this.vansahApiUrl = apiUrl;
    }

    /**
     * Sets the Test Folder Path.
     * @param testFolderPath The folder path for the test folder in Vansah
     */
    public void setTESTFOLDER_PATH(String testFolderPath) {
        if (testFolderPath != null && !testFolderPath.startsWith("/") && testFolderPath.contains("/")) {
            this.testFolderPath = testFolderPath;
        } else {
            System.out.println("Warning: Invalid TESTFOLDER_PATH. Path must contain at least one '/' and must not start with '/'");
        }
    }

    /**
     * Sets the JIRA Issue Key.
     * @param jiraIssueKey The key of the Jira issue
     */
    public void setJIRA_ISSUE_KEY(String jiraIssueKey) {
        this.jiraIssueKey = jiraIssueKey;
    }

    /**
     * Sets the Sprint Name.
     * @param sprintName The name of the sprint
     */
    public void setSPRINT_NAME(String sprintName) {
        this.sprintName = sprintName;
    }

    /**
     * Sets the Release Name.
     * @param releaseName The name of the release
     */
    public void setRELEASE_NAME(String releaseName) {
        this.releaseName = releaseName;
    }

    /**
     * Sets the Environment Name.
     * @param environmentName The name of the environment
     */
    public void setENVIRONMENT_NAME(String environmentName) {
        this.environmentName = environmentName;
    }

    /**
     * Sets the Advanced Test Plan Key.
     * @param testPlanKey The key of the Advanced Test Plan
     */
    public void setAdvancedTestPlanKey(String testPlanKey) {
        this.advancedTestPlanKey = testPlanKey;
    }

    /**
     * Sets the Standard Test Plan Key.
     * @param testPlanKey The key of the Standard Test Plan
     */
    public void setStandardTestPlanKey(String testPlanKey) {
        this.standardTestPlanKey = testPlanKey;
    }

    /**
     * Gets the current test run ID.
     * @return The current test run ID
     */
    public String getCurrentTestRunId() {
        return currentTestRunId;
    }

    /**
     * Creates a new test run linked to a specific JIRA issue.
     * @param testcase The test case identifier linked to the JIRA issue
     * @throws Exception If the API call fails
     */
    public void addTestRunFromJIRAIssue(String testcase) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("testCaseKey", testcase);
        if (jiraIssueKey != null) {
            payload.put("issueKey", jiraIssueKey);
        }
        if (sprintName != null) {
            payload.put("sprintName", sprintName);
        }
        if (releaseName != null) {
            payload.put("releaseName", releaseName);
        }
        if (environmentName != null) {
            payload.put("environmentName", environmentName);
        }

        String response = makeApiCall("POST", "/testrun", payload);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
        if (jsonResponse.has("testRunKey")) {
            currentTestRunId = jsonResponse.get("testRunKey").getAsString();
        }
    }

    /**
     * Creates a new test run within a specified test folder.
     * @param testcase The identifier of the test case
     * @throws Exception If the API call fails
     */
    public void addTestRunFromTestFolder(String testcase) throws Exception {
        if (testFolderPath == null || testFolderPath.isEmpty()) {
            throw new IllegalStateException("TESTFOLDER_PATH must be set before creating test run from test folder");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("testCaseKey", testcase);
        payload.put("testFolderPath", testFolderPath);
        if (sprintName != null) {
            payload.put("sprintName", sprintName);
        }
        if (releaseName != null) {
            payload.put("releaseName", releaseName);
        }
        if (environmentName != null) {
            payload.put("environmentName", environmentName);
        }

        String response = makeApiCall("POST", "/testrun", payload);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
        if (jsonResponse.has("testRunKey")) {
            currentTestRunId = jsonResponse.get("testRunKey").getAsString();
        }
    }

    /**
     * Adds a test run from Advanced Test Plan.
     * @param testPlanAssetType The type of asset (folder or issue)
     * @param testCaseKey The key of the test case
     * @throws Exception If the API call fails
     */
    public void addTestRunFromAdvancedTestPlan(String testPlanAssetType, String testCaseKey) throws Exception {
        if (advancedTestPlanKey == null || advancedTestPlanKey.isEmpty()) {
            throw new IllegalStateException("Advanced Test Plan Key must be set");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("testCaseKey", testCaseKey);
        payload.put("testPlanKey", advancedTestPlanKey);
        payload.put("testPlanAssetType", testPlanAssetType);

        String response = makeApiCall("POST", "/testrun", payload);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
        if (jsonResponse.has("testRunKey")) {
            currentTestRunId = jsonResponse.get("testRunKey").getAsString();
        }
    }

    /**
     * Adds a test run from Standard Test Plan.
     * @param testCaseKey The key of the test case
     * @throws Exception If the API call fails
     */
    public void addTestRunFromStandardTestPlan(String testCaseKey) throws Exception {
        if (standardTestPlanKey == null || standardTestPlanKey.isEmpty()) {
            throw new IllegalStateException("Standard Test Plan Key must be set");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("testCaseKey", testCaseKey);
        payload.put("testPlanKey", standardTestPlanKey);

        String response = makeApiCall("POST", "/testrun", payload);
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
        if (jsonResponse.has("testRunKey")) {
            currentTestRunId = jsonResponse.get("testRunKey").getAsString();
        }
    }

    /**
     * Logs the result of a specific test step.
     * @param result The outcome of the test step (PASSED, FAILED, N/A, NOT_TESTED) or (0=N/A, 1=FAIL, 2=PASS, 3=Not tested)
     * @param comment An optional comment describing the test step outcome
     * @param testStepRow The index of the test step within the test case
     * @throws Exception If the API call fails
     */
    public void addTestLog(String result, String comment, Integer testStepRow) throws Exception {
        addTestLog(result, comment, testStepRow, null);
    }

    /**
     * Logs the result of a specific test step with optional screenshot.
     * @param result The outcome of the test step
     * @param comment An optional comment describing the test step outcome
     * @param testStepRow The index of the test step within the test case
     * @param screenshotFile Optional screenshot file to upload
     * @throws Exception If the API call fails
     */
    public void addTestLog(String result, String comment, Integer testStepRow, File screenshotFile) throws Exception {
        if (currentTestRunId == null || currentTestRunId.isEmpty()) {
            throw new IllegalStateException("No test run created. Call addTestRun method first.");
        }

        String normalizedResult = normalizeResult(result);
        Map<String, Object> payload = new HashMap<>();
        payload.put("testRunKey", currentTestRunId);
        payload.put("result", normalizedResult);
        payload.put("testStepRow", testStepRow);
        if (comment != null && !comment.isEmpty()) {
            payload.put("comment", comment);
        }

        if (screenshotFile != null && screenshotFile.exists()) {
            makeApiCallWithFile("POST", "/testlog", payload, screenshotFile);
        } else {
            makeApiCall("POST", "/testlog", payload);
        }
    }

    /**
     * Logs the result of a specific test step using integer result code.
     * @param result The outcome code (0=N/A, 1=FAIL, 2=PASS, 3=Not tested)
     * @param comment An optional comment describing the test step outcome
     * @param testStepRow The index of the test step within the test case
     * @throws Exception If the API call fails
     */
    public void addTestLog(Integer result, String comment, Integer testStepRow) throws Exception {
        String resultString = convertResultCodeToString(result);
        addTestLog(resultString, comment, testStepRow, null);
    }

    /**
     * Updates an existing test log.
     * @param result The updated result
     * @param comment An optional comment
     * @param screenshotFile Optional screenshot file
     * @throws Exception If the API call fails
     */
    public void updateTestLog(String result, String comment, File screenshotFile) throws Exception {
        if (currentTestRunId == null || currentTestRunId.isEmpty()) {
            throw new IllegalStateException("No test run created.");
        }

        String normalizedResult = normalizeResult(result);
        Map<String, Object> payload = new HashMap<>();
        payload.put("testRunKey", currentTestRunId);
        payload.put("result", normalizedResult);
        if (comment != null && !comment.isEmpty()) {
            payload.put("comment", comment);
        }

        if (screenshotFile != null && screenshotFile.exists()) {
            makeApiCallWithFile("PUT", "/testlog", payload, screenshotFile);
        } else {
            makeApiCall("PUT", "/testlog", payload);
        }
    }

    /**
     * Quickly logs the overall result of a test case associated with a JIRA issue.
     * @param testcase The test case identifier
     * @param result The overall test result (PASSED, FAILED) or (2=PASS, 1=FAIL)
     * @throws Exception If the API call fails
     */
    public void addQuickTestFromJiraIssue(String testcase, int result) throws Exception {
        addTestRunFromJIRAIssue(testcase);
        String resultString = result == 2 ? "PASSED" : "FAILED";
        addTestLog(resultString, "Quick test result", 1);
    }

    /**
     * Quickly logs the overall result of a test case associated with a test folder.
     * @param testcase The test case identifier
     * @param result The overall test result (PASSED, FAILED) or (2=PASS, 1=FAIL)
     * @throws Exception If the API call fails
     */
    public void addQuickTestFromTestFolders(String testcase, int result) throws Exception {
        addTestRunFromTestFolder(testcase);
        String resultString = result == 2 ? "PASSED" : "FAILED";
        addTestLog(resultString, "Quick test result", 1);
    }

    /**
     * Removes the current test run.
     * @throws Exception If the API call fails
     */
    public void removeTestRun() throws Exception {
        if (currentTestRunId == null || currentTestRunId.isEmpty()) {
            throw new IllegalStateException("No test run to remove.");
        }
        makeApiCall("DELETE", "/testrun/" + currentTestRunId, null);
        currentTestRunId = null;
    }

    /**
     * Removes a test log.
     * @throws Exception If the API call fails
     */
    public void removeTestLog() throws Exception {
        if (currentTestRunId == null || currentTestRunId.isEmpty()) {
            throw new IllegalStateException("No test run available.");
        }
        makeApiCall("DELETE", "/testlog/" + currentTestRunId, null);
    }

    /**
     * Makes an API call to Vansah.
     */
    private String makeApiCall(String method, String endpoint, Map<String, Object> payload) throws Exception {
        if (vansahToken == null || vansahToken.isEmpty()) {
            throw new IllegalStateException("Vansah token is not set. Please set VANSAH_TOKEN in .env file or use setVansahToken() method.");
        }

        try {
            String url = vansahApiUrl + endpoint;
            HttpResponse<String> response;

            if ("POST".equals(method)) {
                response = Unirest.post(url)
                        .header("Authorization", "Bearer " + vansahToken)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(payload))
                        .asString();
            } else if ("PUT".equals(method)) {
                response = Unirest.put(url)
                        .header("Authorization", "Bearer " + vansahToken)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(payload))
                        .asString();
            } else if ("DELETE".equals(method)) {
                response = Unirest.delete(url)
                        .header("Authorization", "Bearer " + vansahToken)
                        .asString();
            } else {
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }

            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                return response.getBody();
            } else {
                throw new Exception("API call failed with status " + response.getStatus() + ": " + response.getBody());
            }
        } catch (UnirestException e) {
            throw new Exception("Failed to make API call: " + e.getMessage(), e);
        }
    }

    /**
     * Makes an API call with file upload.
     */
    private String makeApiCallWithFile(String method, String endpoint, Map<String, Object> payload, File file) throws Exception {
        if (vansahToken == null || vansahToken.isEmpty()) {
            throw new IllegalStateException("Vansah token is not set.");
        }

        try {
            String url = vansahApiUrl + endpoint;
            HttpResponse<String> response;

            if ("POST".equals(method)) {
                response = Unirest.post(url)
                        .header("Authorization", "Bearer " + vansahToken)
                        .field("testRunKey", payload.get("testRunKey"))
                        .field("result", payload.get("result"))
                        .field("testStepRow", payload.get("testStepRow"))
                        .field("comment", payload.get("comment"))
                        .field("screenshot", file)
                        .asString();
            } else if ("PUT".equals(method)) {
                response = Unirest.put(url)
                        .header("Authorization", "Bearer " + vansahToken)
                        .field("testRunKey", payload.get("testRunKey"))
                        .field("result", payload.get("result"))
                        .field("comment", payload.get("comment"))
                        .field("screenshot", file)
                        .asString();
            } else {
                throw new IllegalArgumentException("Unsupported HTTP method for file upload: " + method);
            }

            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                return response.getBody();
            } else {
                throw new Exception("API call failed with status " + response.getStatus() + ": " + response.getBody());
            }
        } catch (UnirestException e) {
            throw new Exception("Failed to make API call with file: " + e.getMessage(), e);
        }
    }

    /**
     * Normalizes result string to standard format.
     */
    private String normalizeResult(String result) {
        if (result == null) {
            return "NOT_TESTED";
        }
        String upper = result.toUpperCase();
        if (upper.equals("PASS") || upper.equals("PASSED")) {
            return "PASSED";
        } else if (upper.equals("FAIL") || upper.equals("FAILED")) {
            return "FAILED";
        } else if (upper.equals("NA") || upper.equals("N/A")) {
            return "N/A";
        } else if (upper.equals("NOT_TESTED") || upper.equals("NOT TESTED")) {
            return "NOT_TESTED";
        }
        return upper;
    }

    /**
     * Converts integer result code to string.
     */
    private String convertResultCodeToString(Integer result) {
        if (result == null) {
            return "NOT_TESTED";
        }
        switch (result) {
            case 0:
                return "N/A";
            case 1:
                return "FAILED";
            case 2:
                return "PASSED";
            case 3:
                return "NOT_TESTED";
            default:
                return "NOT_TESTED";
        }
    }
}

