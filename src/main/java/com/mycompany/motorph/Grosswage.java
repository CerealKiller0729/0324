package com.mycompany.motorph;

import java.time.LocalTime;
import java.util.List;
import java.time.LocalDate;

public class Grosswage extends Calculation {
    private final String employeeID;
    private final String employeeName;
    private double gross;
    private double hourlyRate;
    private double hoursWorked;
    private final int year;
    private final int month;
    private final boolean isFirstHalf;
    private final LocalTime shiftStartTime;
    private final boolean nightShift;

    // Detailed breakdown fields
    private double regularHours;
    private double overtimeHours;
    private double regularPay;
    private double overtimePay;
    private double holidayPay;

    public Grosswage(String empId, String firstName, String lastName, int year, 
                   int month, boolean isFirstHalf, LocalTime shiftStartTime, boolean nightShift) {
        if (empId == null || empId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty");
        }
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("Employee name cannot be null");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1-12");
        }
        if (year != 2024) {
            throw new IllegalArgumentException("This implementation only supports 2024 payroll");
        }
        if (shiftStartTime == null) {
            throw new IllegalArgumentException("Shift start time cannot be null");
        }

        this.employeeID = empId;
        this.employeeName = firstName + " " + lastName;
        this.year = year;
        this.month = month;
        this.isFirstHalf = isFirstHalf;
        this.shiftStartTime = shiftStartTime;
        this.nightShift = nightShift;
    }

    @Override
    public double calculate() {
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();
        Employee employee = findEmployeeById(employeeID, employees);
        
        if (employee == null) {
            throw new IllegalStateException("Employee ID " + employeeID + " not found");
        }

        hourlyRate = employee.getHourlyRate();
        if (hourlyRate <= 0) {
            throw new IllegalStateException("Invalid hourly rate for employee");
        }

        hoursWorked = AttendanceRecord.calculateTotalHours(year, month, employeeID, isFirstHalf);
        if (hoursWorked < 0) {
            throw new IllegalStateException("Invalid hours worked calculation");
        }

        calculatePayWithHolidayRates();
        gross = regularPay + overtimePay;
        
        // Validate calculations
        validateHolidayPay();
        
        return gross;
    }

    private void calculatePayWithHolidayRates() {
        resetCounters();
        
        for (AttendanceRecord record : AttendanceRecord.getAttendanceRecords()) {
            if (isRecordForThisPayPeriod(record)) {
                processDailyHours(record);
            }
        }
    }

    private void resetCounters() {
        regularHours = 0;
        overtimeHours = 0;
        regularPay = 0;
        overtimePay = 0;
        holidayPay = 0;
    }

    private boolean isRecordForThisPayPeriod(AttendanceRecord record) {
        return record.getId().equals(employeeID) &&
               record.getDate().getYear() == year &&
               record.getDate().getMonthValue() == month &&
               (record.getDate().getDayOfMonth() <= 15) == isFirstHalf;
    }

    private void processDailyHours(AttendanceRecord record) {
        LocalDate recordDate = record.getDate();
        double dailyHours = record.calculateHoursWorked();
        double dayRegular = Math.min(dailyHours, 8.0);
        double dayOvertime = Math.max(0, dailyHours - 8.0);

        if (HolidayChecker.isHoliday(recordDate)) {
            applyHolidayRates(recordDate, dayRegular, dayOvertime);
        } else {
            applyRegularRates(dayRegular, dayOvertime);
        }
    }

    private void applyHolidayRates(LocalDate date, double regularHrs, double overtimeHrs) {
        double multiplier = HolidayChecker.getHolidayPayMultiplier(date);
        double holidayPremiumRate = multiplier - 1.0;
        
        // Base pay (includes regular 100% pay)
        regularPay += regularHrs * hourlyRate * multiplier;
        
        // Holiday premium (additional pay only)
        holidayPay += regularHrs * hourlyRate * holidayPremiumRate;
        
        if (overtimeHrs > 0) {
            // Standard overtime rate first
            double overtimeRate = nightShift ? 1.10 : 1.25;
            double baseOvertime = overtimeHrs * hourlyRate * overtimeRate;
            
            // Holiday premium for overtime
            double overtimePremium = overtimeHrs * hourlyRate * holidayPremiumRate;
            
            overtimePay += baseOvertime;
            holidayPay += overtimePremium;
        }

        regularHours += regularHrs;
        overtimeHours += overtimeHrs;
    }

    private void applyRegularRates(double regularHrs, double overtimeHrs) {
        regularPay += regularHrs * hourlyRate;
        
        if (overtimeHrs > 0) {
            double overtimeMultiplier = nightShift ? 1.10 : 1.25;
            overtimePay += overtimeHrs * hourlyRate * overtimeMultiplier;
        }
        
        regularHours += regularHrs;
        overtimeHours += overtimeHrs;
    }

    private void validateHolidayPay() {
        // Sanity check - holiday pay shouldn't exceed reasonable limits
        double maxExpectedPremium = 
            (regularHours + overtimeHours) * hourlyRate * 1.3; // 130% premium cap
        
        if (holidayPay > maxExpectedPremium) {
            throw new IllegalStateException(
                String.format("Holiday pay %.2f exceeds reasonable maximum (%.2f)", 
                holidayPay, maxExpectedPremium));
        }
    }

    // Getters
    public double getRegularHours() { return regularHours; }
    public double getOvertimeHours() { return overtimeHours; }
    public double getRegularPay() { return regularPay; }
    public double getOvertimePay() { return overtimePay; }
    public double getHolidayPay() { return holidayPay; }
    public String getEmployeeID() { return employeeID; }
    public String getEmployeeName() { return employeeName; }
    public double getHourlyRate() { return hourlyRate; }
    public double getHoursWorked() { return hoursWorked; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public boolean isFirstHalf() { return isFirstHalf; }
    public LocalTime getShiftStartTime() { return shiftStartTime; }
    public boolean isNightShift() { return nightShift; }

    private Employee findEmployeeById(String employeeId, List<Employee> employees) {
        return employees.stream()
            .filter(e -> e != null && e.getEmployeeNumber().equals(employeeId))
            .findFirst()
            .orElse(null);
    }
    
    public void printCalculationDetails() {
        System.out.println("\nCalculation Details:");
        System.out.printf("Hourly Rate: PHP %.2f%n", hourlyRate);
        System.out.printf("Regular Hours: %.2f (PHP %.2f)%n", 
            regularHours, regularPay);
        System.out.printf("Overtime Hours: %.2f (PHP %.2f)%n", 
            overtimeHours, overtimePay);
        System.out.printf("Holiday Premium Pay: PHP %.2f%n", holidayPay);
        System.out.printf("Total Gross: PHP %.2f%n", gross);
    }
}