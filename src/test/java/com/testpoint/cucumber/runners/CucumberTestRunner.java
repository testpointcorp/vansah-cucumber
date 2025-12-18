package com.testpoint.cucumber.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Cucumber Test Runner for executing feature files.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.testpoint.cucumber.steps", "com.testpoint.cucumber.hooks"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports",
                "json:target/cucumber-reports/Cucumber.json",
                "junit:target/cucumber-reports/Cucumber.xml"
        },
        monochrome = true,
        tags = "@Vansah"
)
public class CucumberTestRunner {
}

