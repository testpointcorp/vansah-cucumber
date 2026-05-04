package com.testpoint.calculator;

public class Calculator {

    private boolean initialized;

    public void initialize() {
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void requireInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Calculator is not initialized");
        }
    }

    public double add(double a, double b) {
        requireInitialized();
        return a + b;
    }

    public double subtract(double a, double b) {
        requireInitialized();
        return a - b;
    }

    public double multiply(double a, double b) {
        requireInitialized();
        return a * b;
    }

    public double divide(double a, double b) {
        requireInitialized();
        if (b == 0) {
            throw new ArithmeticException("Division by zero is not allowed");
        }
        return a / b;
    }
}
