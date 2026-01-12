package com.vansah.app;
/**
 * Calculator utility class
 */
public class SimpleCalculator {

    /**
     * Adds two integers
     * @param num1 first integer
     * @param num2 second integer
     * @return sum of num1 and num2
     */
    public static int add(int num1, int num2) {
        return num1 + num2;
    }

    /**
     * Subtracts two integers and returns the absolute difference
     * @param num1 first integer
     * @param num2 second integer
     * @return absolute difference between num1 and num2
     */
    public static int subtract(int num1, int num2) {
        return Math.abs(num1 - num2);
    }

    /**
     * Divides two integers
     * @param num1 dividend
     * @param num2 divisor
     * @return division result
     * @throws ArithmeticException when divisor is zero
     */
    public static int divide(int num1, int num2) {
        if (num2 == 0) {
            throw new ArithmeticException("Division by zero is not allowed");
        }
        return num1 / num2;
    }
}