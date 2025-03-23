package com.mycompany.motorph;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 *
 * @author angeliquerivera
 */

public class AttendanceRecord {

    private String name;
    private String id;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;
    private static final String XLSX_FILE_PATH = "src/main/resources/AttendanceRecord.xlsx";
   private static final DecimalFormat decimalFormat = new DecimalFormat("#.##"); // Declare DecimalFormat

    public static ArrayList<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DecimalFormat grossWageDecimalFormat = new DecimalFormat("#.##"); // Declare DecimalFormat
    
    // Constructor
    public AttendanceRecord(String name, String id, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        this.name = name;
        this.id = id;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    // New constructor that accepts a String array
    public AttendanceRecord(String[] data) {
        if (data.length < 6) {
            throw new IllegalArgumentException("Insufficient data to create AttendanceRecord");
        }
        this.id = data[0];
        this.name = data[1] + " " + data[2].trim(); // Assuming name is split into two parts
        this.date = LocalDate.parse(data[3], dateFormatter);
        this.timeIn = LocalTime.parse(data[4], timeFormatter);
        this.timeOut = LocalTime.parse(data[5], timeFormatter);
    }

    public AttendanceRecord() {}

    // LOADS ATTENDANCE FROM AN XLSX FILE
    public static void loadAttendanceFromExcel(String filePath) {
        attendanceRecords = loadAttendance(filePath);
        System.out.println("Loaded " + attendanceRecords.size() + " attendance records.");
        
    }

    public static ArrayList<AttendanceRecord> loadAttendance(String filePath) {
        ArrayList<AttendanceRecord> attendanceRecords = new ArrayList<>();
         //DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
         //DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm"); // 24 hour format


        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
             
            // Skip the header row
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    String id = getCellValueAsString(row.getCell(0));
                    String name = getCellValueAsString(row.getCell(1));
                    String surname = getCellValueAsString(row.getCell(2)).trim();
                    
                  
                     LocalDate date;
                if (row.getCell(3).getCellType() == CellType.NUMERIC) {
                    date = row.getCell(3).getLocalDateTimeCellValue().toLocalDate();
                } else {
                    String dateString = getCellValueAsString(row.getCell(3));
                    date = LocalDate.parse(dateString, dateFormatter);
                }
                
                
                 LocalTime timeIn;
                    if (row.getCell(4).getCellType() == CellType.NUMERIC) {
                        timeIn = row.getCell(4).getLocalDateTimeCellValue().toLocalTime();
                    } else {
                        String timeInString = getCellValueAsString(row.getCell(4));
                        timeIn = LocalTime.parse(timeInString, timeFormatter);
                    }

                 LocalTime timeOut;
                    if (row.getCell(5).getCellType() == CellType.NUMERIC) {
                        timeOut = row.getCell(5).getLocalDateTimeCellValue().toLocalTime();
                    } else {
                        String timeOutString = getCellValueAsString(row.getCell(5));
                        timeOut = LocalTime.parse(timeOutString, timeFormatter);
                    }
                   
                    // Create a new AttendanceRecord using the existing constructor
                    attendanceRecords.add(new AttendanceRecord(name + " " + surname, id, date, timeIn, timeOut));
                }
            }
                  
        } catch (IOException e) {
             System.err.println("Error loading attendance records: " + e.getMessage());
        }

        return attendanceRecords;
    }

    // Helper method to get cell value as String
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    // CALCULATES HOURS PER ATTENDANCE RECORD
    double calculateHoursWorked() {
         Duration duration;
    if (timeOut.isBefore(timeIn)) {
        // If timeOut is before timeIn, assume the employee worked past midnight
        duration = Duration.between(timeIn, timeOut.plusHours(24));
    } else {
        duration = Duration.between(timeIn, timeOut);
    }
    return duration.toHours() + (duration.toMinutes() % 60) / 60.0; // Return total hours as a double
}
    

    // TO CALCULATES HOURS WORKED ON A SPECIFIC MONTH OF AN EMPLOYEE
    public static double calculateTotalHoursAndPrint(int year, int month, String targetEmployeeId) {
        double totalHours = 0;
        boolean foundRecord = false;
        
    // Print only the header information
        System.out.println("Checking attendance for Employee ID: " + targetEmployeeId + " for Year: " + year + " Month: " + month);

        for (AttendanceRecord entry : attendanceRecords) {
        // Normalize entry ID for comparison
        if (entry.getId().equals(targetEmployeeId) || entry.getId().equals(targetEmployeeId + ".0")) {
            int entryYear = entry.getDate().getYear();
            int entryMonth = entry.getDate().getMonthValue();

            // Check if the entry matches the specified year and month
            if (entryYear == year && entryMonth == month) {
                foundRecord = true;
                double hoursWorked = entry.calculateHoursWorked();
                totalHours += hoursWorked; // Add to total hours
            }
        }
    }

    // Print the total hours only if records were found
    if (foundRecord) {
        System.out.printf("Total Hours for Employee ID: %s: %.2f%n", targetEmployeeId, totalHours);
    } else {
        System.out.println("No hours found for Employee ID: " + targetEmployeeId);
    }

        return totalHours;
    }
    
    // Getters
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTimeIn() {
        return timeIn;
    }

    public LocalTime getTimeOut() {
        return timeOut;
    }

    public static ArrayList<AttendanceRecord> getAttendanceRecords() {
        return attendanceRecords;
    }
}
