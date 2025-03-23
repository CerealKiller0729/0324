/*
 * Class for calculating the net wage of an employee after deductions.
 */
package com.mycompany.motorph;

public class Netwage extends Calculation {
    private final String employeeID; // Employee ID
    private final String employeeName; // Employee name
    private final double grossWage; // Gross wage
    private final double hoursWorked; // Total hours worked
    private final boolean isFirstHalf; // Flag to indicate if it's the first half of the month

    public Netwage(String employeeID, String employeeName, double grossWage, double hoursWorked, boolean isFirstHalf) {
        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.grossWage = grossWage;
        this.hoursWorked = hoursWorked;
        this.isFirstHalf = isFirstHalf;
    }

    @Override
    public double calculate() {
        // Create an instance of Grosswage to pass to deduction classes
        Grosswage grosswage = new Grosswage(employeeID, "", "", 0, 0, 0, isFirstHalf); // Dummy instance

        // Create instances of each deduction class
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
        double totalDeductions = sssDeduction + philhealthDeduction + pagibigDeduction + lateDeduction;

        // Calculate net wage
        double netWage = grossWage - totalDeductions;

        // Print the net wage along with other details
        printNetWageDetails(sssDeduction, philhealthDeduction, pagibigDeduction, lateDeduction, totalDeductions, withholdingTax, netWage);

        return netWage; // Return the net wage
    }

    private void printNetWageDetails(double sssDeduction, double philhealthDeduction, double pagibigDeduction,
                                     double lateDeduction, double totalDeductions, WithholdingTax withholdingTax, double netWage) {
        double taxableIncome = withholdingTax.getTaxableIncome();
        double tax = withholdingTax.getTax();

        System.out.println("""
                ------------------------------------------
                Employee ID: %s
                Employee Name: %s
                ------------------------------------------
                Total Hours: %s
                Gross Wage: %s

                SSS Deduction: %s
                Philhealth Deduction: %s
                Pag-Ibig Deduction: %s
                Late Deductions: %s

                Total Deductions: %s

                Taxable Income: %s
                Withholding Tax: %s

                Net Wage: %s
                ------------------------------------------
                """.formatted(
                        employeeID,
                        employeeName,
                        format(hoursWorked),
                        format(grossWage),
                        format(sssDeduction),
                        format(philhealthDeduction),
                        format(pagibigDeduction),
                        format(lateDeduction),
                        format(totalDeductions),
                        format(taxableIncome),
                        format(tax),
                        format(netWage)
        ));
    }
}