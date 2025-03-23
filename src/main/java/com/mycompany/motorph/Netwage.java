/*
 * Class for calculating the net wage of an employee after deductions.
 */
package com.mycompany.motorph;

import java.text.DecimalFormat;
/**
 *
 * @author angeliquerivera
 */
public class Netwage extends Calculation {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private final Grosswage grosswage; // Grosswage object for the employee
    private final String employeeID; // Employee ID
    private final String employeeName; // Employee name
    private final double gross; // Gross wage
    private final double hours; // Total hours worked
    private final boolean isFirstHalf; // Flag to indicate if it's the first half of the month

    // Constructor
    public Netwage(String employeeID, String employeeName, double gross, double hours, boolean isFirstHalf) {
        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.gross = gross;
        this.hours = hours;
        this.isFirstHalf = isFirstHalf;

        // Create a Grosswage object using the provided data
        // Note: The Grosswage constructor requires empId, firstName, lastName, hourlyRate, year, month, and isFirstHalf
        // Since we don't have all these details, we pass placeholders for firstName, lastName, hourlyRate, year, and month
        this.grosswage = new Grosswage(employeeID, "", "", 0, 0, 0, isFirstHalf);
    }

    @Override
    public double calculate() {
        // Create instances of each deduction class using the actual Grosswage object
        WithholdingTax withholdingTax = new WithholdingTax(grosswage);
        Calculation sss = new SSS(grosswage);
        Calculation philhealth = new Philhealth(grosswage);
        Calculation pagibig = new Pagibig(grosswage);
        Calculation latePenalty = new LatePenalty();

        // Calculate each deduction
        double sssDeduction = sss.calculate();
        double philhealthDeduction = philhealth.calculate();
        double pagibigDeduction = pagibig.calculate();
        double lateDeduction = latePenalty.calculate();

        // Calculate total deductions
        double totalDeduction = sssDeduction + philhealthDeduction + pagibigDeduction + lateDeduction;

        // Calculate net wage
        double net = gross - totalDeduction;

        // Display results for the current half
        System.out.println("\n" + (isFirstHalf ? "First" : "Second") + " Half of the Month:");
        System.out.println("Total Hours Worked: " + decimalFormat.format(hours));
        System.out.println("Gross Wage: " + decimalFormat.format(gross));
        System.out.println("Net Wage: " + decimalFormat.format(net));

        return net; // Return the net wage
    }

    // Implement the getSSSDeduction method
    public double getSSSDeduction() {
        Calculation sss = new SSS(grosswage);
        return sss.calculate();
    }

    // Implement the getPhilhealthDeduction method
    public double getPhilhealthDeduction() {
        Calculation philhealth = new Philhealth(grosswage);
        return philhealth.calculate();
    }

    // Implement the getPagIbigDeduction method
    public double getPagIbigDeduction() {
        Calculation pagibig = new Pagibig(grosswage);
        return pagibig.calculate();
    }

    // Implement the getLateDeduction method
    public double getLateDeduction() {
        Calculation latePenalty = new LatePenalty();
        return latePenalty.calculate();
    }

    // Implement the getTotalDeductions method
    public double getTotalDeductions() {
        return getSSSDeduction() + getPhilhealthDeduction() + getPagIbigDeduction() + getLateDeduction();
    }

    // Implement the getTaxableIncome method
    public double getTaxableIncome() {
        return gross - getTotalDeductions();
    }

    // Implement the getWithholdingTax method
    public double getWithholdingTax() {
        WithholdingTax withholdingTax = new WithholdingTax(grosswage);
        return withholdingTax.calculate();
    }
}