package com.mycompany.motorph;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author angeliquerivera
 */

/**
 * Class for managing attendance records loaded from an Excel file.
 */
public class AttendanceRecord {

    private String name;
    private String id;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;
    private static final String XLSX_FILE_PATH = "src/main/resources/AttendanceRecord.xlsx";
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public static ArrayList<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Constructor for AttendanceRecord.
     * @param name The name of the employee.
     * @param id The employee ID.
     * @param date The date of attendance.
     * @param timeIn The time the employee clocked in.
     * @param timeOut The time the employee clocked out.
     */
    public AttendanceRecord(String name, String id, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        this.name = name;
        this.id = id;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    /**
     * Constructor for AttendanceRecord using a String array.
     * @param data An array of strings containing attendance data.
     */
    public AttendanceRecord(String[] data) {
        if (data.length < 6) {
            throw new IllegalArgumentException("Insufficient data to create AttendanceRecord");
        }
        this.id = data[0];
        this.name = data[1] + " " + data[2].trim(); // Combine first and last name
        this.date = LocalDate.parse(data[3], dateFormatter);
        this.timeIn = LocalTime.parse(data[4], timeFormatter);
        this.timeOut = LocalTime.parse(data[5], timeFormatter);
    }

    /**
     * Loads attendance records from an Excel file.
     * @param filePath The path to the Excel file.
     */
    public static void loadAttendanceFromExcel(String filePath) {
        try {
            attendanceRecords = loadAttendance(filePath);
            System.out.println("Loaded " + attendanceRecords.size() + " attendance records.");
        } catch (IOException e) {
            System.err.println("Error loading attendance records: " + e.getMessage());
        }
    }

    /**
     * Loads attendance records from an Excel file and returns them as a list.
     * @param filePath The path to the Excel file.
     * @return A list of AttendanceRecord objects.
     * @throws IOException If there is an error reading the file.
     */
    public static ArrayList<AttendanceRecord> loadAttendance(String filePath) throws IOException {
        ArrayList<AttendanceRecord> attendanceRecords = new ArrayList<>();

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

                    LocalDate date = parseDate(row.getCell(3));
                    LocalTime timeIn = parseTime(row.getCell(4));
                    LocalTime timeOut = parseTime(row.getCell(5));

                    attendanceRecords.add(new AttendanceRecord(name + " " + surname, id, date, timeIn, timeOut));
                }
            }
        }

        return attendanceRecords;
    }

    /**
     * Parses a cell value as a LocalDate.
     * @param cell The cell containing the date.
     * @return The parsed LocalDate.
     */
    private static LocalDate parseDate(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Date cell is null.");
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } else {
            String dateString = getCellValueAsString(cell);
            return LocalDate.parse(dateString, dateFormatter);
        }
    }

    /**
     * Parses a cell value as a LocalTime.
     * @param cell The cell containing the time.
     * @return The parsed LocalTime.
     */
    private static LocalTime parseTime(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Time cell is null.");
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getLocalDateTimeCellValue().toLocalTime();
        } else {
            String timeString = getCellValueAsString(cell);
            return LocalTime.parse(timeString, timeFormatter);
        }
    }

    /**
     * Gets the value of a cell as a String.
     * @param cell The cell to read.
     * @return The cell value as a String.
     */
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

    /**
     * Calculates the total hours worked for a specific employee in a given month and year.
     * @param year The year to filter by.
     * @param month The month to filter by.
     * @param targetEmployeeId The employee ID to filter by.
     * @return The total hours worked.
     */
    public static double calculateTotalHours(int year, int month, String targetEmployeeId) {
        double totalHours = 0;
        for (AttendanceRecord entry : attendanceRecords) {
            if (entry.getId().equals(targetEmployeeId) || entry.getId().equals(targetEmployeeId + ".0")) {
                int entryYear = entry.getDate().getYear();
                int entryMonth = entry.getDate().getMonthValue();

                if (entryYear == year && entryMonth == month) {
                    totalHours += entry.calculateHoursWorked();
                }
            }
        }
        return totalHours;
    }

    /**
     * Calculates the total hours worked for a specific employee in a given month and year, for either the first or second half of the month.
     * @param year The year to filter by.
     * @param month The month to filter by.
     * @param targetEmployeeId The employee ID to filter by.
     * @param isFirstHalf Flag to indicate if it's the first half of the month.
     * @return The total hours worked.
     */
    public static double calculateTotalHours(int year, int month, String targetEmployeeId, boolean isFirstHalf) {
        double totalHours = 0;
        for (AttendanceRecord entry : attendanceRecords) {
            if (entry.getId().equals(targetEmployeeId) || entry.getId().equals(targetEmployeeId + ".0")) {
                int entryYear = entry.getDate().getYear();
                int entryMonth = entry.getDate().getMonthValue();
                int entryDay = entry.getDate().getDayOfMonth();

                if (entryYear == year && entryMonth == month) {
                    if ((isFirstHalf && entryDay <= 15) || (!isFirstHalf && entryDay > 15)) {
                        totalHours += entry.calculateHoursWorked();
                    }
                }
            }
        }
        return totalHours;
    }

    /**
     * Prints the total hours worked for a specific employee in a given month and year.
     * @param year The year to filter by.
     * @param month The month to filter by.
     * @param targetEmployeeId The employee ID to filter by.
     */
    public static void printTotalHours(int year, int month, String targetEmployeeId) {
        double totalHours = calculateTotalHours(year, month, targetEmployeeId);
        System.out.printf("Total Hours for Employee ID: %s: %.2f%n", targetEmployeeId, totalHours);
    }

    /**
     * Calculates the hours worked for a single attendance record.
     * @return The total hours worked as a double.
     */
    public double calculateHoursWorked() {
        Duration duration;
        if (timeOut.isBefore(timeIn)) {
            // If timeOut is before timeIn, assume the employee worked past midnight
            duration = Duration.between(timeIn, timeOut.plusHours(24));
        } else {
            duration = Duration.between(timeIn, timeOut);
        }
        return duration.toHours() + (duration.toMinutes() % 60) / 60.0; // Return total hours as a double
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