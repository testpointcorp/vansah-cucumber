package com.testpoint.vansah.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Configuration class for Vansah integration.
 * Loads configuration from .env file.
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

    public String getVansahToken() {
        // Official guide often uses CONNECT_DEMO_TOKEN; keep backwards-compat with VANSAH_TOKEN
        String connectToken = getEnv("CONNECT_DEMO_TOKEN");
        if (connectToken != null && !connectToken.isEmpty()) return connectToken;
        return getEnv("VANSAH_TOKEN");
    }

    public String getVansahApiUrl() {
        // Official binding defaults to https://prod.vansah.com and builds /api/v1/... endpoints on top.
        // Keep backwards-compat with older VANSAH_API_URL config key.
        String vansahUrl = getEnv("VANSAH_URL");
        if (vansahUrl != null && !vansahUrl.isEmpty()) return vansahUrl;
        return getEnv("VANSAH_API_URL", "https://prod.vansah.com");
    }

    public String getProjectKey() {
        return getEnv("VANSAH_PROJECT_KEY", getEnv("PROJECT_KEY"));
    }

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

