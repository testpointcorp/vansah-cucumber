package com.testpoint.cucumber.steps;

import com.testpoint.cucumber.hooks.VansahHooks;
import com.testpoint.vansah.VansahNode;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

/**
 * Example step definitions for Cucumber tests.
 * These steps demonstrate how to log test step results to Vansah.
 */
public class ExampleSteps {
    private VansahNode vansahNode;
    private int stepCounter = 0;

    public ExampleSteps() {
        this.vansahNode = VansahHooks.getVansahNode();
    }

    @Given("I have a test scenario")
    public void iHaveATestScenario() {
        stepCounter++;
        logStepToVansah("PASSED", "Given step executed successfully", stepCounter);
    }

    @When("I perform an action")
    public void iPerformAnAction() {
        stepCounter++;
        logStepToVansah("PASSED", "When step executed successfully", stepCounter);
    }

    @Then("I verify the result")
    public void iVerifyTheResult() {
        stepCounter++;
        logStepToVansah("PASSED", "Then step executed successfully", stepCounter);
    }

    @Given("I have a failing scenario")
    public void iHaveAFailingScenario() {
        stepCounter++;
        logStepToVansah("PASSED", "Given step executed", stepCounter);
    }

    @When("I perform an action that fails")
    public void iPerformAnActionThatFails() {
        stepCounter++;
        logStepToVansah("FAILED", "When step failed intentionally", stepCounter);
        throw new AssertionError("Intentional failure for testing");
    }

    @Then("I verify the result fails")
    public void iVerifyTheResultFails() {
        stepCounter++;
        // This step won't execute if previous step fails
        logStepToVansah("NOT_TESTED", "Then step not executed due to previous failure", stepCounter);
    }

    /**
     * Helper method to log test steps to Vansah.
     */
    private void logStepToVansah(String result, String comment, int stepRow) {
        if (vansahNode != null && vansahNode.getCurrentTestRunId() != null) {
            try {
                vansahNode.addTestLog(result, comment, stepRow);
            } catch (Exception e) {
                System.err.println("Failed to log step to Vansah: " + e.getMessage());
            }
        }
    }
}

