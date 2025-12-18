package com.testpoint.cucumber.hooks;

import com.testpoint.vansah.VansahNode;
import com.testpoint.vansah.config.VansahConfig;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Cucumber hooks for integrating test execution with Vansah.
 * These hooks automatically create test runs and log results to Vansah.
 */
public class VansahHooks {
    private static VansahNode vansahNode;
    private static Map<String, String> scenarioTestRunMap = new HashMap<>();
    private static VansahConfig config = VansahConfig.getInstance();

    @Before
    public void beforeScenario(Scenario scenario) {
        try {
            if (vansahNode == null) {
                vansahNode = new VansahNode();
                vansahNode.setVansahToken(config.getVansahToken());
                vansahNode.setVansahApiUrl(config.getVansahApiUrl());

                // Set optional configurations if available
                String jiraIssueKey = config.getJiraIssueKey();
                if (jiraIssueKey != null && !jiraIssueKey.isEmpty()) {
                    vansahNode.setJIRA_ISSUE_KEY(jiraIssueKey);
                }

                String testFolderPath = config.getTestFolderPath();
                if (testFolderPath != null && !testFolderPath.isEmpty()) {
                    vansahNode.setTESTFOLDER_PATH(testFolderPath);
                }

                String advancedTestPlanKey = config.getAdvancedTestPlanKey();
                if (advancedTestPlanKey != null && !advancedTestPlanKey.isEmpty()) {
                    vansahNode.setAdvancedTestPlanKey(advancedTestPlanKey);
                }

                String standardTestPlanKey = config.getStandardTestPlanKey();
                if (standardTestPlanKey != null && !standardTestPlanKey.isEmpty()) {
                    vansahNode.setStandardTestPlanKey(standardTestPlanKey);
                }
            }

            // Extract test case key from scenario tags or name
            String testCaseKey = extractTestCaseKey(scenario);
            if (testCaseKey != null && !testCaseKey.isEmpty()) {
                createTestRun(testCaseKey);
                scenarioTestRunMap.put(scenario.getId(), testCaseKey);
            }
        } catch (Exception e) {
            System.err.println("Error in Vansah before hook: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        try {
            String testCaseKey = scenarioTestRunMap.get(scenario.getId());
            if (testCaseKey != null && vansahNode != null && vansahNode.getCurrentTestRunId() != null) {
                // Log overall scenario result
                String result = scenario.isFailed() ? "FAILED" : "PASSED";
                String comment = scenario.isFailed() 
                    ? "Scenario failed: " + scenario.getName() 
                    : "Scenario passed: " + scenario.getName();

                // Log error details if scenario failed
                if (scenario.isFailed() && scenario.getError() != null) {
                    comment += "\nError: " + scenario.getError().getMessage();
                }

                // Log screenshot if available
                File screenshot = null;
                if (scenario.isFailed() && scenario.attach != null) {
                    // Screenshot handling would be done here if needed
                }

                vansahNode.addTestLog(result, comment, 1, screenshot);
            }
        } catch (Exception e) {
            System.err.println("Error in Vansah after hook: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extracts test case key from scenario tags or name.
     * Looks for tags starting with @TC- or @TESTCASE- or uses scenario name.
     */
    private String extractTestCaseKey(Scenario scenario) {
        // Check tags for test case key (e.g., @TC-PROJ-123)
        for (String tag : scenario.getSourceTagNames()) {
            if (tag.startsWith("@TC-")) {
                return tag.substring(4); // Remove @TC- prefix
            } else if (tag.startsWith("@TESTCASE-")) {
                return tag.substring(10); // Remove @TESTCASE- prefix
            }
        }

        // If no tag found, try to extract from scenario name
        String scenarioName = scenario.getName();
        // Look for pattern like "TC-PROJ-123" or "PROJ-123" in name
        if (scenarioName != null) {
            String[] parts = scenarioName.split("\\s+");
            for (String part : parts) {
                if (part.matches("^[A-Z]+-\\d+$")) {
                    return part;
                }
            }
        }

        return null;
    }

    /**
     * Creates a test run in Vansah based on configuration.
     */
    private void createTestRun(String testCaseKey) throws Exception {
        String advancedTestPlanKey = config.getAdvancedTestPlanKey();
        String standardTestPlanKey = config.getStandardTestPlanKey();
        String testFolderPath = config.getTestFolderPath();
        String jiraIssueKey = config.getJiraIssueKey();

        if (advancedTestPlanKey != null && !advancedTestPlanKey.isEmpty()) {
            // Determine asset type based on available configuration
            String assetType = (testFolderPath != null && !testFolderPath.isEmpty()) ? "folder" : "issue";
            vansahNode.addTestRunFromAdvancedTestPlan(assetType, testCaseKey);
        } else if (standardTestPlanKey != null && !standardTestPlanKey.isEmpty()) {
            vansahNode.addTestRunFromStandardTestPlan(testCaseKey);
        } else if (testFolderPath != null && !testFolderPath.isEmpty()) {
            vansahNode.addTestRunFromTestFolder(testCaseKey);
        } else if (jiraIssueKey != null && !jiraIssueKey.isEmpty()) {
            vansahNode.addTestRunFromJIRAIssue(testCaseKey);
        } else {
            // Default to JIRA Issue if no specific configuration
            vansahNode.addTestRunFromJIRAIssue(testCaseKey);
        }
    }

    /**
     * Gets the VansahNode instance for use in step definitions.
     */
    public static VansahNode getVansahNode() {
        return vansahNode;
    }
}

