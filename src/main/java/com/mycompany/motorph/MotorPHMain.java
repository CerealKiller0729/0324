/*
 * Main class for MotorPH application.
 * Handles user login, menu navigation, and core functionalities.
 */
package com.mycompany.motorph;

import java.util.List;
import java.util.Scanner;
import java.text.DecimalFormat;

public class MotorPHMain {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public static void main(String[] args) {
        // Attempt to log in before proceeding to the main menu
        if (login()) {
            System.out.println("Current Working Directory: " + System.getProperty("user.dir"));

            // Load attendance records from the Excel file
            try {
                AttendanceRecord.loadAttendanceFromExcel("src/main/resources/AttendanceRecord.xlsx");
                System.out.println("Attendance records loaded successfully.");
            } catch (Exception e) {
                System.err.println("Error loading attendance records: " + e.getMessage());
                return; // Exit if attendance records cannot be loaded
            }

            // Display the main menu
            menu();
        } else {
            System.out.println("Login failed. Exiting application.");
        }
    }

    private static boolean login() {
        String correctUsername = "admin";
        String correctPassword = "admin";

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        // Normalize input to lowercase for case-insensitive comparison
        return username.equalsIgnoreCase(correctUsername) && password.equalsIgnoreCase(correctPassword);
    }

    private static void menu() {
        int resume = 1;
        do {
            System.out.print("""
                    ----- DASHBOARD-----
                    1: Show Employee Details
                    2: Calculate Gross Wage
                    3: Calculate Net Wage
                    0: EXIT
                    -------------------------
                    CHOOSE: """);

            String choice = scanner.next();
            System.out.println("-------------------------");

            switch (choice) {
                case "1" -> handleEmployeeDetails();
                case "2" -> calculateGrossWage();
                case "3" -> calculateNetWage();
                case "0" -> {
                    System.out.println("Exiting application. Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid input! Please try again.");
            }

            System.out.println("Back to menu? 1 = yes, 0 = no");
            resume = scanner.nextInt();
        } while (resume != 0);
    }

    private static void handleEmployeeDetails() {
        System.out.print("""
                ----- DASHBOARD-----
                1: Individual Employee Details
                2: All Employee Details
                -------------------------
                Choose: """);

        String detailSub = scanner.next();
        System.out.println("-------------------------");

        switch (detailSub) {
            case "1" -> printEmployeeDetails();
            case "2" -> printAllEmployeeDetails();
            default -> System.out.println("Invalid input! Please try again.");
        }
    }

    private static void printEmployeeDetails() {
        System.out.print("Enter Employee #: ");
        String empNum = scanner.next();
        System.out.println("-------------------------");

        Employee employee = findEmployeeById(empNum);
        if (employee != null) {
            System.out.println("Employee Details for Employee ID " + empNum + ":" + '\n' +
                    "-------------------------");
            System.out.println(employee.toString(true));
            System.out.println("-------------------------");
        } else {
            System.out.println("Employee ID " + empNum + " not found.");
        }
    }

    private static void printAllEmployeeDetails() {
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();
        String format = "%-15s %-20s %-20s"; // Format for displaying employee details

        System.out.println("-------------------------");
        System.out.println("|     Employee List     |");
        System.out.println("-------------------------");

        for (Employee employee : employees) {
            System.out.printf(format, employee.getEmployeeNumber(), employee.getLastName(), employee.getFirstName());
            System.out.println(); // Print a new line
        }

        System.out.println("-------------------------");
    }

    /**
     * Calculates and displays the gross wage for a specific employee for both halves of the month.
     */
    private static void calculateGrossWage() {
        System.out.print("Enter Employee #: ");
        String empId = scanner.next();

        // Find the employee by ID
        Employee employee = findEmployeeById(empId);
        if (employee == null) {
            System.out.println("Employee not found.");
            return;
        }

        // Retrieve the employee's name
        String firstName = employee.getFirstName();
        String lastName = employee.getLastName();
        String employeeName = lastName + ", " + firstName;

        // Display the employee's name
        System.out.println("Employee Name: " + employeeName);

        int year = getYear();
        int month = getMonth();

        // Calculate gross wage and hours worked for the first half of the month
        Grosswage grosswageFirstHalf = new Grosswage(empId, firstName, lastName, 0, year, month, true);
        double grossFirstHalf = grosswageFirstHalf.calculate();
        double hoursFirstHalf = grosswageFirstHalf.getHoursWorked();

        // Calculate gross wage and hours worked for the second half of the month
        Grosswage grosswageSecondHalf = new Grosswage(empId, firstName, lastName, 0, year, month, false);
        double grossSecondHalf = grosswageSecondHalf.calculate();
        double hoursSecondHalf = grosswageSecondHalf.getHoursWorked();

        // Display results for both halves
        System.out.println("\nFirst Half of the Month:");
        System.out.println("Total Hours Worked: " + decimalFormat.format(hoursFirstHalf));
        System.out.println("Gross Wage: " + decimalFormat.format(grossFirstHalf));

        System.out.println("\nSecond Half of the Month:");
        System.out.println("Total Hours Worked: " + decimalFormat.format(hoursSecondHalf));
        System.out.println("Gross Wage: " + decimalFormat.format(grossSecondHalf));
    }

    /**
     * Calculates and displays the net wage for a specific employee for both halves of the month.
     */
    private static void calculateNetWage() {
        System.out.print("Enter Employee #: ");
        String empId = scanner.next();

        // Find the employee by ID
        Employee employee = findEmployeeById(empId);
        if (employee == null) {
            System.out.println("Employee not found.");
            return;
        }

        // Retrieve the employee's name
        String firstName = employee.getFirstName();
        String lastName = employee.getLastName();
        String employeeName = lastName + ", " + firstName;

        // Display the employee's name
        System.out.println("Employee Name: " + employeeName);

        int year = getYear();
        int month = getMonth();

        // Calculate gross wage and hours worked for the first half of the month
        Grosswage grosswageFirstHalf = new Grosswage(empId, firstName, lastName, 0, year, month, true);
        double grossFirstHalf = grosswageFirstHalf.calculate();
        double hoursFirstHalf = grosswageFirstHalf.getHoursWorked();

        // Calculate net wage for the first half of the month
        Netwage netwageFirstHalf = new Netwage(empId, employeeName, grossFirstHalf, hoursFirstHalf, true);
        double netFirstHalf = netwageFirstHalf.calculate();

        // Calculate gross wage and hours worked for the second half of the month
        Grosswage grosswageSecondHalf = new Grosswage(empId, firstName, lastName, 0, year, month, false);
        double grossSecondHalf = grosswageSecondHalf.calculate();
        double hoursSecondHalf = grosswageSecondHalf.getHoursWorked();

        // Calculate net wage for the second half of the month
        Netwage netwageSecondHalf = new Netwage(empId, employeeName, grossSecondHalf, hoursSecondHalf, false);
        double netSecondHalf = netwageSecondHalf.calculate();

        // Display results for both halves
        System.out.println("\nFirst Half of the Month:");
        System.out.println("Total Hours Worked: " + decimalFormat.format(hoursFirstHalf));
        System.out.println("Gross Wage: " + decimalFormat.format(grossFirstHalf));
        System.out.println("Net Wage: " + decimalFormat.format(netFirstHalf));

        System.out.println("\nSecond Half of the Month:");
        System.out.println("Total Hours Worked: " + decimalFormat.format(hoursSecondHalf));
        System.out.println("Gross Wage: " + decimalFormat.format(grossSecondHalf));
        System.out.println("Net Wage: " + decimalFormat.format(netSecondHalf));
    }

    private static String getEmployeeId() {
        System.out.print("Enter Employee #: ");
        return scanner.next();
    }

    private static int getYear() {
        System.out.print("Enter Year: ");
        return scanner.nextInt();
    }

    private static int getMonth() {
        int month;
        while (true) {
            System.out.print("Enter Month: ");
            month = scanner.nextInt();
            if (month >= 1 && month <= 12) {
                break; // Valid month
            } else {
                System.out.print("Invalid month. Please enter a month between 1 and 12: ");
            }
        }
        return month;
    }

    private static Employee findEmployeeById(String empId) {
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();
        for (Employee employee : employees) {
            if (employee.getEmployeeNumber().equals(empId) || employee.getEmployeeNumber().equals(empId + ".0")) {
                return employee;
            }
        }
        return null; // Return null if not found
    }
}
