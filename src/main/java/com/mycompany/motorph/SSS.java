/*
 * Class for calculating SSS (Social Security System) deductions based on the employee's gross wage.
 */
package com.mycompany.motorph;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SSS extends Calculation {

    private double sssDeduction; // SSS deduction amount
    private final Grosswage grosswage; // Gross wage object for calculation

    private static final String XLSX_FILE_PATH = "src/main/resources/SSSCont.xlsx"; // Path to the SSS contributions Excel file
    private static final List<SSSRecord> sssDeductionRecords; // List of SSS deduction records

    // Static block to load SSS deduction records when the class is loaded
    static {
        sssDeductionRecords = loadSssDeductions();
        if (sssDeductionRecords == null) {
            throw new RuntimeException("Failed to load SSS deductions.");
        }
    }

    /**
     * Constructor for SSS.
     * @param grosswage The Grosswage object containing the employee's gross wage.
     */
    public SSS(Grosswage grosswage) {
        this.grosswage = grosswage;
    }

    /**
     * Calculates the SSS deduction based on the employee's gross wage.
     * @return The SSS deduction amount.
     */
    @Override
    public double calculate() {
        double gross = grosswage.calculate();

        // Iterate through every compensation range to find the appropriate contribution
        for (SSSRecord record : sssDeductionRecords) {
            double[] range = parseSssCompensationRange(record.getCompensationRange());
            if (gross > range[0] && gross <= range[1]) {
                sssDeduction = record.getContribution();
                break; // Exit loop once the correct range is found
            }
        }

        return sssDeduction;
    }

    /**
     * Loads SSS deduction records from an Excel file.
     * @return A list of SSSRecord objects.
     */
    private static List<SSSRecord> loadSssDeductions() {
        List<SSSRecord> deductionRecords = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(XLSX_FILE_PATH);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            // Skip the header row
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    String compensationRange = getCellValueAsString(row.getCell(0)); // Compensation range
                    double contribution = row.getCell(1).getNumericCellValue(); // Contribution amount

                    // Create a new SSSRecord object and add it to the list
                    deductionRecords.add(new SSSRecord(compensationRange, contribution));
                }
            }
        } catch (IOException e) {
            handleException(e);
        }

        return deductionRecords;
    }

    /**
     * Helper method to get the value of a cell as a String.
     * @param cell The cell to retrieve the value from.
     * @return The cell value as a String.
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * Parses the SSS compensation range string into a numeric range.
     * @param compensationRange The compensation range string (e.g., "0-1000").
     * @return An array containing the start and end values of the range.
     */
    private static double[] parseSssCompensationRange(String compensationRange) {
        compensationRange = compensationRange.trim(); // Remove extra spaces

        // Split the range by hyphen
        String[] rangeParts = compensationRange.split("-");

        // Validate the range format
        if (rangeParts.length != 2) {
            throw new IllegalArgumentException("Invalid compensation range format: " + compensationRange);
        }

        try {
            double start = Double.parseDouble(rangeParts[0].trim());
            double end = Double.parseDouble(rangeParts[1].trim());
            return new double[]{start, end};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric format in compensation range: " + compensationRange, e);
        }
    }

    /**
     * Handles exceptions by printing the stack trace.
     * @param e The exception to handle.
     */
    private static void handleException(Exception e) {
        e.printStackTrace();
    }

    /**
     * Returns the SSS deduction amount.
     * @return The SSS deduction amount.
     */
    public double getSssDeduction() {
        return sssDeduction;
    }
}