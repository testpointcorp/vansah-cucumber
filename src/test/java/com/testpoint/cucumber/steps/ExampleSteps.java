package com.testpoint.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Example step definitions for Cucumber scenarios.
 */
public class ExampleSteps {

    @Given("I have a test scenario")
    public void iHaveATestScenario() {
        System.out.println("Setting up test scenario...");
    }

    @When("I perform an action")
    public void iPerformAnAction() {
        System.out.println("Performing action...");
    }

    @Then("I verify the result")
    public void iVerifyTheResult() {
        System.out.println("Verifying result - PASSED");
    }

    @Given("I have a failing test")
    public void iHaveAFailingTest() {
        System.out.println("Setting up failing test scenario...");
    }

    @When("I perform an action that fails")
    public void iPerformAnActionThatFails() {
        System.out.println("Performing action that will fail...");
    }

    @Then("I should see an error")
    public void iShouldSeeAnError() {
        // Uncomment to simulate failure:
        // throw new AssertionError("Expected error occurred");
        System.out.println("Error handling verified");
    }

    @Given("I have another scenario")
    public void iHaveAnotherScenario() {
        System.out.println("Setting up another test scenario...");
    }

    @When("I perform another action")
    public void iPerformAnotherAction() {
        System.out.println("Performing another action...");
    }

    @Then("I verify another result")
    public void iVerifyAnotherResult() {
        System.out.println("Verifying another result - PASSED");
    }

    @Given("I have a third scenario")
    public void iHaveAThirdScenario() {
        System.out.println("Setting up third test scenario...");
    }

    @When("I perform a third action")
    public void iPerformAThirdAction() {
        System.out.println("Performing third action...");
    }

    @Then("I verify third result")
    public void iVerifyThirdResult() {
        System.out.println("Verifying third result - PASSED");
    }
}
