package com.testpoint.cucumber.steps;

import com.testpoint.calculator.Calculator;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExampleSteps {

    private Calculator calculator;
    private Double firstInput;
    private Double secondInput;
    private Double result;
    private Exception caughtException;
    private double[] perOperationResults;

    @Before
    public void resetState() {
        calculator = null;
        firstInput = null;
        secondInput = null;
        result = null;
        caughtException = null;
        perOperationResults = null;
    }

    @Given("the calculator is initialized")
    public void theCalculatorIsInitialized() {
        calculator = new Calculator();
        calculator.initialize();
        assertTrue("Calculator should be initialized", calculator.isInitialized());
    }

    @Given("the user enters the first numeric input")
    public void theUserEntersTheFirstNumericInput() {
        firstInput = 12.0;
    }

    @Given("the user enters the second numeric input")
    public void theUserEntersTheSecondNumericInput() {
        secondInput = 4.0;
    }

    @Given("the user enters the second numeric input \\(not zero)")
    public void theUserEntersTheSecondNumericInputNotZero() {
        secondInput = 4.0;
        assertTrue("Second input must not be zero", secondInput != 0.0);
    }

    @Given("the user enters a numeric input as the dividend")
    public void theUserEntersANumericInputAsTheDividend() {
        firstInput = 10.0;
    }

    @Given("the user enters zero as the divisor")
    public void theUserEntersZeroAsTheDivisor() {
        secondInput = 0.0;
    }

    @Given("the user enters two numeric inputs")
    public void theUserEntersTwoNumericInputs() {
        firstInput = 8.0;
        secondInput = 2.0;
    }

    @Given("the user enters two valid numeric inputs")
    public void theUserEntersTwoValidNumericInputs() {
        firstInput = 20.0;
        secondInput = 5.0;
    }

    @When("the user selects the addition operation")
    public void theUserSelectsTheAdditionOperation() {
        result = calculator.add(firstInput, secondInput);
    }

    @When("the user selects the subtraction operation")
    public void theUserSelectsTheSubtractionOperation() {
        result = calculator.subtract(firstInput, secondInput);
    }

    @When("the user selects the multiplication operation")
    public void theUserSelectsTheMultiplicationOperation() {
        result = calculator.multiply(firstInput, secondInput);
    }

    @When("the user selects the division operation")
    public void theUserSelectsTheDivisionOperation() {
        try {
            result = calculator.divide(firstInput, secondInput);
        } catch (ArithmeticException e) {
            caughtException = e;
        }
    }

    @When("the user attempts to enter two numeric inputs for any operation")
    public void theUserAttemptsToEnterTwoNumericInputsForAnyOperation() {
        firstInput = 3.0;
        secondInput = 7.0;
    }

    @When("the user selects each arithmetic operation \\(+, -, *, \\/) in turn")
    public void theUserSelectsEachArithmeticOperationInTurn() {
        perOperationResults = new double[]{
            calculator.add(firstInput, secondInput),
            calculator.subtract(firstInput, secondInput),
            calculator.multiply(firstInput, secondInput),
            calculator.divide(firstInput, secondInput)
        };
    }

    @When("the user selects any arithmetic operation \\(+, -, *, \\/) except division by zero")
    public void theUserSelectsAnyArithmeticOperationExceptDivisionByZero() {
        perOperationResults = new double[]{
            calculator.add(firstInput, secondInput),
            calculator.subtract(firstInput, secondInput),
            calculator.multiply(firstInput, secondInput),
            calculator.divide(firstInput, secondInput)
        };
    }

    @Then("the calculator returns the correct sum of the two inputs")
    public void theCalculatorReturnsTheCorrectSumOfTheTwoInputs() {
        assertNotNull(result);
        assertEquals(firstInput + secondInput, result, 1e-9);
    }

    @Then("the calculator returns the correct difference of the two inputs")
    public void theCalculatorReturnsTheCorrectDifferenceOfTheTwoInputs() {
        assertNotNull(result);
        assertEquals(firstInput - secondInput, result, 1e-9);
    }

    @Then("the calculator returns the correct product of the two inputs")
    public void theCalculatorReturnsTheCorrectProductOfTheTwoInputs() {
        assertNotNull(result);
        assertEquals(firstInput * secondInput, result, 1e-9);
    }

    @Then("the calculator returns the correct quotient of the two inputs")
    public void theCalculatorReturnsTheCorrectQuotientOfTheTwoInputs() {
        assertNotNull(result);
        assertEquals(firstInput / secondInput, result, 1e-9);
    }

    @Then("the calculator returns an appropriate error message or exception indicating division by zero is not allowed")
    public void theCalculatorReturnsAnErrorForDivisionByZero() {
        if (caughtException == null) {
            fail("Expected an ArithmeticException for division by zero, but none was thrown");
        }
        assertTrue(caughtException instanceof ArithmeticException);
        assertTrue(caughtException.getMessage().toLowerCase().contains("zero"));
    }

    @Then("the calculator accepts both numeric inputs without error")
    public void theCalculatorAcceptsBothNumericInputsWithoutError() {
        assertNotNull(firstInput);
        assertNotNull(secondInput);
        calculator.add(firstInput, secondInput);
    }

    @Then("the calculator returns the correct result for each selected operation")
    public void theCalculatorReturnsTheCorrectResultForEachSelectedOperation() {
        assertNotNull(perOperationResults);
        assertEquals(4, perOperationResults.length);
        assertEquals(firstInput + secondInput, perOperationResults[0], 1e-9);
        assertEquals(firstInput - secondInput, perOperationResults[1], 1e-9);
        assertEquals(firstInput * secondInput, perOperationResults[2], 1e-9);
        assertEquals(firstInput / secondInput, perOperationResults[3], 1e-9);
    }

    @Then("the calculator returns the correct result for the selected operation")
    public void theCalculatorReturnsTheCorrectResultForTheSelectedOperation() {
        assertNotNull(perOperationResults);
        assertEquals(firstInput + secondInput, perOperationResults[0], 1e-9);
        assertEquals(firstInput - secondInput, perOperationResults[1], 1e-9);
        assertEquals(firstInput * secondInput + 1, perOperationResults[2], 1e-9);
        assertEquals(firstInput / secondInput, perOperationResults[3], 1e-9);
    }
}
