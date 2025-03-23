/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.motorph;

import java.util.List;
import java.util.Scanner;
import java.text.DecimalFormat;

/**
 *
 * @author angeliquerivera
 */

public class MotorPHMain {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##"); // For formatting output
     
    public static void main(String[] args) {
         // Call the login method before proceeding to the main menu
        if (login()) {
        System.out.println("Current Working Directory: " + System.getProperty("user.dir"));

        // Load attendance records from the Excel file
        AttendanceRecord.loadAttendanceFromExcel("src/main/resources/AttendanceRecord.xlsx");
        System.out.println("Attendance records loaded.");
        menu();
      }  else {
        System.out.println("Login failed. Exiting application.");
        } 
    }    
         // Method to handle user login
    private static boolean login() {
        String correctUsername = "admin";
        String correctPassword = "admin";

        System.out.print("Username: ");
        String username = scanner.nextLine().trim(); // Read username
        System.out.print("Password: ");
        String password = scanner.nextLine().trim(); // Read password
        // Normalize input to lowercase for case-insensitive comparison
        return username.equalsIgnoreCase(correctUsername) && password.equalsIgnoreCase(correctPassword); // Login successful
        
    }

          // Print main menu
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

            String detailSub;
            String ch = scanner.next();

            switch (ch) {
                case "1" -> {
                    System.out.print("""
                                 ----- DASHBOARD-----
                                                            
                                 1: Individual Employee Details
                                 2: All Employee Details
                                 -------------------------
                                 Choose: """);
                    detailSub = scanner.next();
                    System.out.println("-------------------------");
                    menu(detailSub);
                   }

                case "2" -> calculateGrossWage();

                case "3" -> calculateNetWage();
                case "0" -> System.exit(0);

                default -> System.out.println("Invalid Input!");
            }

            System.out.println("Back to menu? 1 = yes, 0 = no");
            resume = scanner.nextInt();
        } while (resume != 0);
    }

    
     // Method to calculate gross wage
    private static void calculateGrossWage() {
        System.out.print("Enter Employee #: ");
        String empId = scanner.next();
    
       // Get year and month only once
       int year = getYear();
       int month = getMonth();

        double totalHours = AttendanceRecord.calculateTotalHoursAndPrint(year, month, empId);
        Employee employee = findEmployeeById(empId); // Method to find employee by ID
        if (employee != null) {
            double hourlyRate = employee.getHourlyRate();
            Grosswage grosswage = new Grosswage(empId, employee.getFirstName(), employee.getLastName(), hourlyRate);
            double grossWage = grosswage.calculate();

            // Output the results
           System.out.println("------------------------------------------");
            System.out.println("Employee ID: " + employee.getEmployeeNumber());
            System.out.println("Name: " + employee.getFirstName() + " " + employee.getLastName());
            System.out.println("Hourly Rate: " + decimalFormat.format(hourlyRate));
            System.out.println("Total Hours: " + decimalFormat.format(totalHours));
            System.out.println("Gross Wage: " + decimalFormat.format(grossWage));
            System.out.println("------------------------------------------");
        } else {
            System.out.println("Employee not found.");
        }
    }

     // Method to calculate net wage
    private static void calculateNetWage() {
         String empId = getEmployeeId();
        int year = getYear();
        int month = getMonth();

       double totalHours = AttendanceRecord.calculateTotalHoursAndPrint(year, month, empId);
       Employee employee = findEmployeeById(empId); // Find employee to get their name
       if (employee != null) {
        double hourlyRate = employee.getHourlyRate();
        double grossWage = hourlyRate * totalHours; // Calculate gross wage based on hours worked

        
        // Create an instance of Netwage
         Netwage netwage = new Netwage(empId, employee.getFirstName() + " " + employee.getLastName(), grossWage, totalHours);
        double net = netwage.calculate(); // Calculate net wage
        System.out.println("Net Wage for Employee ID " + empId + ": " + decimalFormat.format(net));
    } else {
        System.out.println("Employee not found.");
    }
   }
    
        // Method to get employee ID
    private static String getEmployeeId() {
        System.out.print("Enter Employee #: ");
        return scanner.next();
    }

    // Method to get year input
    private static int getYear() {
        System.out.print("Enter Year: ");
        return scanner.nextInt();
    }

    // Method to get month input
    private static int getMonth() {
        System.out.print("Enter Month: ");
        int month;
        while (true) {
            month = scanner.nextInt();
            if (month >= 1 && month <= 12) {
                break; // Valid month
            } else {
                System.out.print("Invalid month. Please enter a month between 1 and 12: ");
            }
        }
        return month;
    }

    // Method to find employee by ID
    private static Employee findEmployeeById(String empId) {
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();
        for (Employee employee : employees) {
            
            if (employee.getEmployeeNumber().equals(empId) || employee.getEmployeeNumber().equals(empId + ".0")) {
                return employee;
            }
        }
        return null; // Return null if not found
    }

     
    // OVERLOAD MENU FOR SUBMENU IN PRINTING EMPLOYEE DETAILS
    private static void menu(String detailSub) {
        switch (detailSub) {
            case "1" -> printEmpSelectList();
            case "2" -> allEmployeeList();
        }
    }

    private static void printEmpSelectList() {
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();

        
        // Prompt for employee number first
        System.out.print("Enter Employee #: ");
        String empNum = scanner.next();
        System.out.println("-------------------------");

        // Search for the employee and display details
        for (Employee employee : employees) {
            if (employee.getEmployeeNumber().equals(empNum)) {
                System.out.println("Employee Details for Employee ID " + empNum + ":" + '\n' +
                        "-------------------------");
                System.out.println(employee.toString(true));
                System.out.println("-------------------------");
                return;
            }
        }

        System.out.println("Employee ID " + empNum + " not found.");
    }

        

    private static void allEmployeeList() {
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();
       // Adjust the format to include first name and last name
       String format = "%-15s %-20s %-20s"; // Adjust the width as needed
           
           System.out.println("-------------------------");
           System.out.println("|     Employee List     |");
           System.out.println("-------------------------");

        for (Employee employee : employees) {
            
             System.out.printf(format, employee.getEmployeeNumber(), employee.getLastName(), employee.getFirstName());
             System.out.println(); // Print a new line
        }

        System.out.println("-------------------------");
    }
}
