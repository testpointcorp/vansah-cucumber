package com.testpoint.cucumber.hooks;

import com.testpoint.vansah.config.VansahConfig;
import com.vansah.VansahNode;

import io.cucumber.java.After;
import io.cucumber.java.Scenario;

/**
 * Cucumber hooks for integrating test execution with Vansah.
 * Minimal showcase: sends ONE result per scenario using Vansah "quick test" methods.
 */
public class VansahHooks {
    private static VansahNode vansahNode;
    private static final VansahConfig config = VansahConfig.getInstance();

    @After
    public void afterScenario(Scenario scenario) {
        try {
            ensureVansahConfigured();

            String testCaseKey = extractTestCaseKey(scenario);
            if (testCaseKey == null || testCaseKey.isEmpty()) return;

            int resultCode = scenario.isFailed() ? 1 : 2; // 1=FAIL, 2=PASS (per guida binding)

            // Prefer "folder path" integration for this showcase; fallback to Jira issue if provided.
            String testFolderPath = config.getTestFolderPath();
            String jiraIssueKey = config.getJiraIssueKey();

            if (testFolderPath != null && !testFolderPath.isEmpty()) {
                vansahNode.addQuickTestFromTestFolders(testCaseKey, resultCode);
                return;
            }

            if (jiraIssueKey != null && !jiraIssueKey.isEmpty()) {
                // Note: addQuickTestFromJiraIssue uses the issue context inside Vansah (see official binding).
                vansahNode.addQuickTestFromJiraIssue(testCaseKey, resultCode);
            }
        } catch (Exception e) {
            System.err.println("Error in Vansah after hook: " + e.getMessage());
        }
    }

    private static void ensureVansahConfigured() {
        if (vansahNode != null) return;
        vansahNode = new VansahNode();

        VansahNode.setVansahToken(config.getVansahToken());
        VansahNode.setVansahURL(config.getVansahApiUrl());
        VansahNode.setProjectKey(config.getProjectKey());

        String testFolderPath = config.getTestFolderPath();
        if (testFolderPath != null && !testFolderPath.isEmpty()) {
            vansahNode.setFOLDERPATH(testFolderPath);
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

        return null;
    }
}

