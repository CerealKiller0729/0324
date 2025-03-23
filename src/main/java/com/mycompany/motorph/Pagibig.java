/*
 * Class for calculating Pag-IBIG deductions.
 */
package com.mycompany.motorph;
/**
 *
 * @author angeliquerivera
 */
public class Pagibig extends Calculation {
    private double pagibigDeduction; // Pag-IBIG deduction amount
    private final Grosswage grosswage; // Gross wage object for calculation

    /**
     * Constructor for Pagibig.
     * @param grosswage The Grosswage object containing the employee's gross wage.
     */
    public Pagibig(Grosswage grosswage) {
        this.grosswage = grosswage;
    }

    /**
     * Calculates the Pag-IBIG deduction based on the employee's gross wage.
     * @return The Pag-IBIG deduction amount.
     */
    @Override
    public double calculate() {
        double gross = grosswage.calculate(); // Get the gross wage from the Grosswage object

        // Calculate Pag-IBIG deduction based on gross wage range
        double pagibig;
        if (gross > 1000.00 && gross <= 1500.00) {
            pagibig = gross * 0.03; // 3% deduction for gross wage between 1000 and 1500
        } else {
            pagibig = gross * 0.04; // 4% deduction for gross wage above 1500
        }

        // Ensure the deduction does not exceed the maximum amount of 100
        if (pagibig > 100) {
            pagibig = 100;
        }

        // Store the Pag-IBIG deduction value and return it
        pagibigDeduction = pagibig;
        return pagibigDeduction;
    }

    /**
     * Returns the Pag-IBIG deduction amount.
     * @return The Pag-IBIG deduction amount.
     */
    public double getPagibigDeduction() {
        return pagibigDeduction;
    }
}