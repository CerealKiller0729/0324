/*
 * Class for calculating PhilHealth (Philippine Health Insurance Corporation) deductions.
 */
package com.mycompany.motorph;
/**
 *
 * @author angeliquerivera
 */

public class Philhealth extends Calculation {
    private double philhealthDeduction; // PhilHealth deduction amount
    private final Grosswage grosswage; // Gross wage object for calculation

    /**
     * Constructor for Philhealth.
     * @param grosswage The Grosswage object containing the employee's gross wage.
     */
    public Philhealth(Grosswage grosswage) {
        this.grosswage = grosswage;
    }

    /**
     * Calculates the PhilHealth deduction based on the employee's gross wage.
     * @return The PhilHealth deduction amount.
     */
    @Override
    public double calculate() {
        double gross = grosswage.calculate(); // Get the gross wage from the Grosswage object

        // Calculate PhilHealth deduction
        double philDed;
        if (gross > 60000) {
            philDed = 1800; // Maximum deduction for gross wage above 60000
        } else {
            philDed = (gross * 0.03) / 2; // 3% deduction divided by 2 (employee and employer share)
        }

        // Store the PhilHealth deduction value and return it
        philhealthDeduction = philDed;
        return philhealthDeduction;
    }

    /**
     * Returns the PhilHealth deduction amount.
     * @return The PhilHealth deduction amount.
     */
    public double getPhilhealthDeduction() {
        return philhealthDeduction;
    }
}