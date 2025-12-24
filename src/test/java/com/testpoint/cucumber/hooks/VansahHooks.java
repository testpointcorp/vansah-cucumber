package com.testpoint.cucumber.hooks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import com.testpoint.vansah.config.VansahConfig;
import com.vansah.VansahNode;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Cucumber hooks for integrating test execution with Vansah.
 * 
 * Features:
 * - Quick test mode: ONE result per scenario (default)
 * - Step-level reporting: detailed results for each step
 * - Automatic screenshots on failure
 * - Support for Test Folders, Jira Issues, Advanced/Standard Test Plans
 */
public class VansahHooks {
    private static VansahNode vansahNode;
    private static final VansahConfig config = VansahConfig.getInstance();
    
    // Per-scenario state
    private String currentTestCaseKey;
    private boolean testRunCreated = false;
    private final AtomicInteger stepCounter = new AtomicInteger(0);
    private boolean scenarioHasFailedStep = false;
    
    // Screenshot directory
    private static final String SCREENSHOT_DIR = "target/screenshots";

    @Before
    public void beforeScenario(Scenario scenario) {
        // Reset state for new scenario
        currentTestCaseKey = extractTestCaseKey(scenario);
        testRunCreated = false;
        stepCounter.set(0);
        scenarioHasFailedStep = false;
        
        if (currentTestCaseKey == null || currentTestCaseKey.isEmpty()) {
            System.out.println("⚠️ No test case key found for scenario: " + scenario.getName());
            return;
        }
        
        ensureVansahConfigured();
        
        // If step-level reporting is enabled, create test run at scenario start
        if (config.isStepLevelReportingEnabled()) {
            createTestRun();
        }
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        if (!config.isStepLevelReportingEnabled()) return;
        if (currentTestCaseKey == null || currentTestCaseKey.isEmpty()) return;
        if (!testRunCreated) return;
        
        try {
            int currentStep = stepCounter.incrementAndGet();
            boolean stepFailed = scenario.isFailed() && !scenarioHasFailedStep;
            
            // Determine result: 2=PASS, 1=FAIL
            int resultCode = stepFailed ? 1 : 2;
            String comment = stepFailed 
                ? "Step failed: " + getLastStepName(scenario) 
                : "Step passed";
            
            // If step failed, capture screenshot
            File screenshot = null;
            if (stepFailed && config.isScreenshotOnFailureEnabled()) {
                screenshot = captureScreenshot(scenario, currentStep);
                scenarioHasFailedStep = true;
            }
            
            // Send step result to Vansah
            if (screenshot != null && screenshot.exists()) {
                vansahNode.addTestLog(resultCode, comment, currentStep, screenshot);
                System.out.println("📸 Screenshot attached for step " + currentStep);
            } else {
                vansahNode.addTestLog(resultCode, comment, currentStep);
            }
            
            System.out.println("📝 Step " + currentStep + " logged: " + (resultCode == 2 ? "PASSED" : "FAILED"));
            
        } catch (Exception e) {
            System.err.println("Error logging step to Vansah: " + e.getMessage());
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        try {
            if (currentTestCaseKey == null || currentTestCaseKey.isEmpty()) return;
            
            ensureVansahConfigured();
            
            // If step-level reporting is enabled, we already sent individual step results
            // Just log the scenario completion
            if (config.isStepLevelReportingEnabled() && testRunCreated) {
                String status = scenario.isFailed() ? "FAILED" : "PASSED";
                System.out.println("✅ Scenario completed: " + status + " (steps logged individually)");
                return;
            }
            
            // Quick test mode: send ONE result per scenario
            int resultCode = scenario.isFailed() ? 1 : 2; // 1=FAIL, 2=PASS
            
            sendQuickTestResult(resultCode);
            
            String status = scenario.isFailed() ? "FAILED" : "PASSED";
            System.out.println("✅ Quick test result sent: " + status);
            
        } catch (Exception e) {
            System.err.println("Error in Vansah after hook: " + e.getMessage());
        }
    }

    /**
     * Creates a test run based on configured integration type.
     * Priority: Advanced Test Plan > Standard Test Plan > Test Folder > Jira Issue
     */
    private void createTestRun() {
        try {
            String advancedTestPlanKey = config.getAdvancedTestPlanKey();
            String standardTestPlanKey = config.getStandardTestPlanKey();
            String testFolderPath = config.getTestFolderPath();
            String jiraIssueKey = config.getJiraIssueKey();
            
            // Priority 1: Advanced Test Plan
            if (isNotEmpty(advancedTestPlanKey)) {
                vansahNode.setAdvancedTestPlanKey(advancedTestPlanKey);
                String assetType = isNotEmpty(testFolderPath) ? "folder" : "issue";
                vansahNode.addTestRunFromAdvancedTestPlan(assetType, currentTestCaseKey);
                testRunCreated = true;
                System.out.println("🚀 Test run created (Advanced Test Plan: " + advancedTestPlanKey + ")");
                return;
            }
            
            // Priority 2: Standard Test Plan
            if (isNotEmpty(standardTestPlanKey)) {
                vansahNode.setStandardTestPlanKey(standardTestPlanKey);
                vansahNode.addTestRunFromStandardTestPlan(currentTestCaseKey);
                testRunCreated = true;
                System.out.println("🚀 Test run created (Standard Test Plan: " + standardTestPlanKey + ")");
                return;
            }
            
            // Priority 3: Test Folder
            if (isNotEmpty(testFolderPath)) {
                vansahNode.addTestRunFromTestFolder(currentTestCaseKey);
                testRunCreated = true;
                System.out.println("🚀 Test run created (Test Folder: " + testFolderPath + ")");
                return;
            }
            
            // Priority 4: Jira Issue
            if (isNotEmpty(jiraIssueKey)) {
                vansahNode.addTestRunFromJIRAIssue(currentTestCaseKey);
                testRunCreated = true;
                System.out.println("🚀 Test run created (Jira Issue: " + jiraIssueKey + ")");
                return;
            }
            
            System.err.println("⚠️ No integration context configured (folder/issue/test plan)");
            
        } catch (Exception e) {
            System.err.println("Error creating test run: " + e.getMessage());
        }
    }

    /**
     * Sends a quick test result (single result per scenario).
     */
    private void sendQuickTestResult(int resultCode) throws Exception {
        String advancedTestPlanKey = config.getAdvancedTestPlanKey();
        String standardTestPlanKey = config.getStandardTestPlanKey();
        String testFolderPath = config.getTestFolderPath();
        String jiraIssueKey = config.getJiraIssueKey();
        
        // For quick tests with Test Plans, create run then update with result
        if (isNotEmpty(advancedTestPlanKey) || isNotEmpty(standardTestPlanKey)) {
            createTestRun();
            if (testRunCreated) {
                // Add a single log entry with the overall result
                vansahNode.addTestLog(resultCode, "Scenario result", 1);
            }
            return;
        }
        
        // Priority: Test Folder > Jira Issue
        if (isNotEmpty(testFolderPath)) {
            vansahNode.addQuickTestFromTestFolders(currentTestCaseKey, resultCode);
            return;
        }
        
        if (isNotEmpty(jiraIssueKey)) {
            vansahNode.addQuickTestFromJiraIssue(currentTestCaseKey, resultCode);
            return;
        }
        
        System.err.println("⚠️ No integration context configured for quick test");
    }

    /**
     * Captures a screenshot for failed steps.
     * Note: In real Selenium/Playwright tests, replace this with actual screenshot capture.
     * 
     * Example with Selenium WebDriver:
     * <pre>
     * byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
     * Files.write(screenshotFile.toPath(), screenshot);
     * return screenshotFile;
     * </pre>
     */
    private File captureScreenshot(Scenario scenario, int stepNumber) {
        try {
            // Create screenshot directory if needed
            Path screenshotDir = Path.of(SCREENSHOT_DIR);
            if (!Files.exists(screenshotDir)) {
                Files.createDirectories(screenshotDir);
            }
            
            String filename = String.format("%s_step%d_%d.png",
                sanitizeFilename(scenario.getName()),
                stepNumber,
                System.currentTimeMillis()
            );
            
            // In a real implementation, capture screenshot here and return the file
            // For this showcase, we log and return null (no actual WebDriver)
            System.out.println("📷 Screenshot capture triggered for: " + filename);
            
            // TODO: Implement actual screenshot capture with your WebDriver
            // File screenshotFile = screenshotDir.resolve(filename).toFile();
            // byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            // Files.write(screenshotFile.toPath(), screenshot);
            // return screenshotFile;
            
            return null;
            
        } catch (Exception e) {
            System.err.println("Error capturing screenshot: " + e.getMessage());
            return null;
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
        
        String jiraIssueKey = config.getJiraIssueKey();
        if (jiraIssueKey != null && !jiraIssueKey.isEmpty()) {
            vansahNode.setJIRA_ISSUE_KEY(jiraIssueKey);
        }
        
        // Set optional properties
        String sprintName = config.getSprintName();
        if (isNotEmpty(sprintName)) {
            vansahNode.setSPRINT_NAME(sprintName);
        }
        
        String releaseName = config.getReleaseName();
        if (isNotEmpty(releaseName)) {
            vansahNode.setRELEASE_NAME(releaseName);
        }
        
        String environmentName = config.getEnvironmentName();
        if (isNotEmpty(environmentName)) {
            vansahNode.setENVIRONMENT_NAME(environmentName);
        }
    }

    /**
     * Extracts test case key from scenario tags.
     * Supports multiple formats:
     * - @TC-PROJ-C1 → PROJ-C1
     * - @TESTCASE-PROJ-C1 → PROJ-C1
     * - @PROJ-C1 → PROJ-C1 (direct test case key format)
     */
    private String extractTestCaseKey(Scenario scenario) {
        for (String tag : scenario.getSourceTagNames()) {
            if (tag.startsWith("@TC-")) {
                return tag.substring(4);
            } else if (tag.startsWith("@TESTCASE-")) {
                return tag.substring(10);
            } else if (tag.matches("@[A-Z]+-C\\d+")) {
                // Direct test case key format: @PROJ-C1, @TD-C1, etc.
                return tag.substring(1);
            }
        }
        return null;
    }

    private String getLastStepName(Scenario scenario) {
        // Cucumber doesn't expose step name directly in AfterStep
        // We use the scenario name as context
        return "Step " + stepCounter.get() + " in " + scenario.getName();
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]", "_").substring(0, Math.min(name.length(), 50));
    }

    private static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
