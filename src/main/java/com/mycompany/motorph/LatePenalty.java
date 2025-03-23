/*
 * Class for calculating late penalties for employees based on their attendance records.
 */
package com.mycompany.motorph;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LatePenalty extends Calculation {
    private String targetEmployeeID; // Employee ID for whom the penalty is calculated
    private int targetMonth; // Target month for calculation
    private double hourlyRate; // Hourly rate of the employee

    /**
     * Constructor for LatePenalty.
     * @param targetEmployeeID The ID of the employee.
     * @param targetMonth The target month for calculation.
     */
    public LatePenalty(String targetEmployeeID, int targetMonth) {
        this.targetEmployeeID = targetEmployeeID;
        this.targetMonth = targetMonth;
    }

    /**
     * Default constructor for LatePenalty.
     */
    public LatePenalty() {
        // Default constructor
    }

    /**
     * Calculates the total late penalty for the employee in the target month.
     * @return The total late penalty amount.
     */
    @Override
    public double calculate() {
        double totalLateDeduction = 0;
        boolean foundLateRecord = false; // Flag to check if any late records were found

        // Load attendance records from the Excel file
        List<AttendanceRecord> attendanceRecords = loadAttendanceFromExcel("src/main/resources/AttendanceRecord.xlsx");

        // Iterate through every attendance record
        for (AttendanceRecord attendanceRecord : attendanceRecords) {
            // Check if the record is for the target employee
            if (attendanceRecord.getId().equals(targetEmployeeID)) {
                LocalTime timeIn = attendanceRecord.getTimeIn();
                int recordMonth = attendanceRecord.getDate().getMonthValue();

                // Check if the record is in the target month
                if (recordMonth == targetMonth) {
                    // Assuming late penalty starts from 8:10 AM (490 minutes) onwards
                    final int lateThreshold = 490;

                    // Convert time in to minutes
                    int lateTime = timeIn.getHour() * 60 + timeIn.getMinute();

                    // Check if the employee is late
                    if (lateTime >= lateThreshold) {
                        // Calculate the per-minute equivalent of the hourly rate
                        double perMinuteRate = hourlyRate / 60.0;

                        // Calculate the deduction amount based on late time
                        double deduction = perMinuteRate * (lateTime - lateThreshold);

                        // Ensure deduction is non-negative
                        totalLateDeduction += Math.max(0, deduction);
                        foundLateRecord = true; // Set the flag to true since we found a late record
                    }
                }
            }
        }

        // Print the target employee ID and month only if a late record was found
        if (foundLateRecord) {
            System.out.println("Late ID: " + targetEmployeeID);
            System.out.println("Month: " + targetMonth);
        }

        return totalLateDeduction; // Return the total late deduction
    }

    /**
     * Loads attendance records from an Excel file.
     * @param filePath The path to the Excel file.
     * @return A list of AttendanceRecord objects.
     */
    private List<AttendanceRecord> loadAttendanceFromExcel(String filePath) {
        List<AttendanceRecord> attendanceRecords = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            // Skip the header row
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    String[] data = new String[row.getLastCellNum()];

                    // Iterate through each cell in the row
                    for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                        Cell cell = row.getCell(cellIndex);
                        data[cellIndex] = (cell != null) ? cell.toString() : ""; // Convert cell to string
                    }

                    // Create a new AttendanceRecord object and add it to the list
                    attendanceRecords.add(new AttendanceRecord(data));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading attendance records: " + e.getMessage());
        }

        return attendanceRecords; // Return the list of attendance records
    }

    /**
     * Sets the hourly rate for the employee.
     * @param hourlyRate The hourly rate to set.
     */
    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    /**
     * Returns the target employee ID.
     * @return The target employee ID.
     */
    public String getTargetEmployeeID() {
        return targetEmployeeID;
    }

    /**
     * Returns the target month.
     * @return The target month.
     */
    public int getTargetMonth() {
        return targetMonth;
    }

    /**
     * Returns the hourly rate.
     * @return The hourly rate.
     */
    public double getHourlyRate() {
        return hourlyRate;
    }
}