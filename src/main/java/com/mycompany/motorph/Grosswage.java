/*
 * Class for calculating the gross wage of an employee based on hours worked and hourly rate.
 */
package com.mycompany.motorph;
/**
 *
 * @author angeliquerivera
 */

import java.util.List;

public class Grosswage extends Calculation {
    private final String employeeID; // Employee ID
    private final String employeeName; // Employee name
    private double gross; // Gross wage
    private double hourlyRate; // Hourly rate
    private double hoursWorked; // Total hours worked
    private int year; // Year for calculation
    private int month; // Month for calculation
    private boolean isFirstHalf; // Flag to indicate if it's the first half of the month

    public Grosswage(String empId, String firstName, String lastName, double hourlyRate, int year, int month, boolean isFirstHalf) {
        this.employeeID = empId;
        this.employeeName = firstName + " " + lastName;
        this.hourlyRate = hourlyRate;
        this.year = year;
        this.month = month;
        this.isFirstHalf = isFirstHalf;
    }

    @Override
    public double calculate() {
        // Get the list of employees
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();

        // Find the employee by ID
        Employee employee = findEmployeeById(employeeID, employees);
        if (employee == null) {
            System.out.println("Employee ID " + employeeID + " not found.");
            return 0; // Exit if the employee is not found
        }

        // Retrieve the hourly rate
        hourlyRate = employee.getHourlyRate();
        if (hourlyRate <= 0) {
            System.out.println("Invalid hourly rate for Employee ID " + employeeID + ": " + hourlyRate);
            return 0; // Exit if the hourly rate is invalid
        }

        System.out.println("Hourly Rate: " + format(hourlyRate));

        // Calculate total hours worked for the first or second half of the month
        hoursWorked = AttendanceRecord.calculateTotalHours(year, month, employeeID, isFirstHalf);

        // Calculate gross wage
        gross = calculateGrossWage(hoursWorked);

        // Print the results
        printGross();

        return gross; // Return the gross wage
    }

    private Employee findEmployeeById(String employeeId, List<Employee> employees) {
        for (Employee employee : employees) {
            if (employee.getEmployeeNumber().equals(employeeId)) {
                return employee; // Return the found employee
            }
        }
        return null; // Return null if not found
    }

    private double calculateGrossWage(double totalHours) {
        return totalHours * hourlyRate; // Gross wage = hours worked * hourly rate
    }

    public void printGross() {
        System.out.println("""
                ------------------------------------------
                Employee ID: %s
                Name: %s
                Hourly Rate: %s
                Total Hours: %s
                Gross Wage: %s
                ------------------------------------------
                """.formatted(
                        employeeID,
                        employeeName,
                        format(hourlyRate),
                        format(hoursWorked),
                        format(gross)
                ));
    }

    // Getters
    public String getEmployeeID() {
        return employeeID;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public double getGross() {
        return gross;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }
}