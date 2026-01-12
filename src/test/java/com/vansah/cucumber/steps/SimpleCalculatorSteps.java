package com.vansah.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import com.vansah.app.SimpleCalculator;
import io.cucumber.java.en.*;
import static org.junit.Assert.*;

public class SimpleCalculatorSteps {

    private int num1;
    private int num2;
    private int actualResult;

    @Given("I have two numbers {int} and {int}")
    public void i_have_two_numbers(int number1, int number2) {
        this.num1 = number1;
        this.num2 = number2;
    }

    @When("I add the numbers")
    public void i_add_the_numbers() {
        actualResult = SimpleCalculator.add(num1, num2);
    }

    @When("I subtract the numbers")
    public void i_subtract_the_numbers() {
        actualResult = SimpleCalculator.subtract(num1, num2);
    }

    @When("I divide the numbers")
    public void i_divide_the_numbers() {
        actualResult = SimpleCalculator.divide(num1, num2);
    }

    @Then("the result should be {int}")
    public void the_result_should_be(int expectedResult) {
        assertEquals(expectedResult, actualResult);
    }

    @Then("the result should not be {int}")
    public void the_result_should_not_be(int unexpectedResult) {
        assertNotEquals(unexpectedResult, actualResult);
    }
}
