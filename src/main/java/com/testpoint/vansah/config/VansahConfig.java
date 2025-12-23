package com.testpoint.vansah.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Configuration class for Vansah integration.
 * Loads configuration from .env file or environment variables.
 * 
 * Required:
 * - CONNECT_DEMO_TOKEN or VANSAH_TOKEN: API authentication token
 * - VANSAH_PROJECT_KEY or PROJECT_KEY: Jira project key
 * 
 * Integration context (at least one required):
 * - TEST_FOLDER_PATH: Test folder path in Vansah
 * - JIRA_ISSUE_KEY: Jira issue to link test runs
 * - ADVANCED_TEST_PLAN_KEY: Advanced Test Plan key
 * - STANDARD_TEST_PLAN_KEY: Standard Test Plan key
 * 
 * Optional features:
 * - STEP_LEVEL_REPORTING: Enable per-step reporting (true/false)
 * - SCREENSHOT_ON_FAILURE: Capture screenshots on failure (true/false)
 * - SPRINT_NAME: Sprint name for test run properties
 * - RELEASE_NAME: Release/version name
 * - ENVIRONMENT_NAME: Environment name (e.g., "UAT", "SYS")
 */
public class VansahConfig {
    private static Dotenv dotenv;
    private static VansahConfig instance;

    private VansahConfig() {
        try {
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        } catch (Exception e) {
            System.out.println("Warning: Could not load .env file. Using system environment variables.");
        }
    }

    public static VansahConfig getInstance() {
        if (instance == null) {
            instance = new VansahConfig();
        }
        return instance;
    }

    // ==================== Core Configuration ====================

    public String getVansahToken() {
        String connectToken = getEnv("CONNECT_DEMO_TOKEN");
        if (connectToken != null && !connectToken.isEmpty()) return connectToken;
        return getEnv("VANSAH_TOKEN");
    }

    public String getVansahApiUrl() {
        String vansahUrl = getEnv("VANSAH_URL");
        if (vansahUrl != null && !vansahUrl.isEmpty()) return vansahUrl;
        return getEnv("VANSAH_API_URL", "https://prod.vansah.com");
    }

    public String getProjectKey() {
        return getEnv("VANSAH_PROJECT_KEY", getEnv("PROJECT_KEY"));
    }

    // ==================== Integration Context ====================

    public String getJiraIssueKey() {
        return getEnv("JIRA_ISSUE_KEY");
    }

    public String getTestFolderPath() {
        return getEnv("TEST_FOLDER_PATH");
    }

    public String getAdvancedTestPlanKey() {
        return getEnv("ADVANCED_TEST_PLAN_KEY");
    }

    public String getStandardTestPlanKey() {
        return getEnv("STANDARD_TEST_PLAN_KEY");
    }

    // ==================== Feature Flags ====================

    /**
     * Enable step-level reporting (sends result for each step, not just scenario).
     * Default: false (quick test mode - one result per scenario)
     */
    public boolean isStepLevelReportingEnabled() {
        String value = getEnv("STEP_LEVEL_REPORTING", "false");
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * Enable automatic screenshot capture on step/scenario failure.
     * Default: false
     */
    public boolean isScreenshotOnFailureEnabled() {
        String value = getEnv("SCREENSHOT_ON_FAILURE", "false");
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    // ==================== Test Run Properties ====================

    /**
     * Sprint name to associate with test runs.
     */
    public String getSprintName() {
        return getEnv("SPRINT_NAME");
    }

    /**
     * Release/version name to associate with test runs.
     */
    public String getReleaseName() {
        return getEnv("RELEASE_NAME");
    }

    /**
     * Environment name (e.g., "UAT", "SYS", "PROD") to associate with test runs.
     */
    public String getEnvironmentName() {
        return getEnv("ENVIRONMENT_NAME");
    }

    // ==================== Helper Methods ====================

    private String getEnv(String key) {
        return getEnv(key, null);
    }

    private String getEnv(String key, String defaultValue) {
        if (dotenv != null) {
            String value = dotenv.get(key);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        String systemValue = System.getenv(key);
        if (systemValue != null && !systemValue.isEmpty()) {
            return systemValue;
        }
        return defaultValue;
    }
}
