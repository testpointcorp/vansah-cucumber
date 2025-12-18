package com.testpoint.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

/**
 * Minimal step definitions for the showcase project.
 * Vansah reporting happens in {@link com.testpoint.cucumber.hooks.VansahHooks}.
 */
public class ExampleSteps {
    @Given("I have a test scenario")
    public void iHaveATestScenario() {
    }

    @When("I perform an action")
    public void iPerformAnAction() {
    }

    @Then("I verify the result")
    public void iVerifyTheResult() {
        // Keep deterministic: if we reached here, scenario is considered "passed".
    }
}

