package com.vansah.cucumber.runners;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * Cucumber Test Runner - Generates JSON report for Vansah import.
 * 
 * Workflow:
 * 1. Run: mvn test
 * 2. Import: npx vansah-cucumber-import -r target/cucumber-reports/cucumber.json [options]
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.vansah.cucumber.steps"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "junit:target/cucumber-reports/cucumber.xml"
        },
        monochrome = true
)
public class CucumberTestRunner {
}
