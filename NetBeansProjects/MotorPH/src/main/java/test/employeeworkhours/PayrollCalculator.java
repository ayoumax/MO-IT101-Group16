/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package test.employeeworkhours;

/**
 * Payroll Calculator Module - Computes Gross Pay, Taxable Pay, and Net Pay.
 * 
 * Reads employee salary details, weekly work hours, and overtime from Excel files.
 * Generates and displays payroll summary.
 * 
 * @author Group16;Arriesgado.M,Marquez.N,Lozada.K,Aquino.N
 */


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;



public class PayrollCalculator {
    
    /**
     * Run method to execute payroll calculations.
     */
    
    public void run() {
        System.out.println("💰 Calculating Payroll...");
        main(null);
    }
    
    public static void main(String[] args) {
        System.out.println("Running Weekly Deductions...");
        String employeeFile = "employee_details.xlsx";
        String weeklyHoursFile = "weekly_hours.xlsx";
        
        // Read employee details
        Map<String, Employee> employees = readEmployeeDetails(employeeFile);

        // Read weekly hours worked & OT
        Map<String, List<WeeklyRecord>> weeklyRecords = readWeeklyHours(weeklyHoursFile);

        // Display payroll
        for (String empID : employees.keySet()) {
            Employee emp = employees.get(empID);
            List<WeeklyRecord> records = weeklyRecords.getOrDefault(empID, new ArrayList<>());
                   
            System.out.printf(" %-15s : %-10s \n", "Employee ID", emp.employeeID);
            System.out.printf(" %-15s : %-25s \n", "Employee Name", emp.firstName + " " + emp.lastName);
            System.out.printf(" %-15s : %-10.2f \n", "Hourly Rate", emp.hourlyRate);
                    
            System.out.printf(" %-12s  %-15s  %-18s  %-15s  %-10s  %-10s \n",
                    "Week", "Weekly Hours", "Weekly Gross Pay", "Taxable Pay", "OT Pay", "Net Pay");
            System.out.println("-----------------------------------------------------------------------------------------");

            for (WeeklyRecord record : records) {
                double weeklyGrossPay = record.hoursWorked * emp.hourlyRate;
                double taxablePay = weeklyGrossPay - emp.governmentDeductions;
                double otPay = 0;
                if (record.otHours > 0) {  // Only compute OT Pay if OT Hours > 0 (Not Late)
                    otPay = (emp.hourlyRate * 1.25 * record.otHours) / 8;  // ✅ OT Pay Calculation
                }
                double weeklyAllowance = (emp.riceAllowance + emp.phoneAllowance + emp.clothingAllowance) / 4;
                double netPay = weeklyGrossPay - emp.weeklyGrossDeduction + weeklyAllowance + otPay;

                System.out.printf(" %-12s  %-15.2f  %-18.2f  %-15.2f  %-10.2f  %-10.2f \n",
                        record.weekStart, record.hoursWorked, weeklyGrossPay, taxablePay, otPay, netPay);
            }
            System.out.println("===========================================================================================\n");
        }
    }

    // Read Employee Details from Excel
    private static Map<String, Employee> readEmployeeDetails(String filePath) {
        Map<String, Employee> employees = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String empID = getCellValue(row.getCell(0));
                String lastName = getCellValue(row.getCell(1));
                String firstName = getCellValue(row.getCell(2));
                double hourlyRate = getNumericCellValue(row.getCell(4));
                double governmentDeductions = getNumericCellValue(row.getCell(5));
                double weeklyGrossDeduction = getNumericCellValue(row.getCell(6));
                double riceAllowance = getNumericCellValue(row.getCell(7));
                double phoneAllowance = getNumericCellValue(row.getCell(8));
                double clothingAllowance = getNumericCellValue(row.getCell(9));

                employees.put(empID, new Employee(empID, lastName, firstName, hourlyRate, governmentDeductions,
                        weeklyGrossDeduction, riceAllowance, phoneAllowance, clothingAllowance));
            }
        } catch (IOException e) {
            System.err.println("❌ ERROR: Failed to read Excel file: " + filePath);
            e.printStackTrace();
        }
        return employees;
    }

    // Read Weekly Hours Worked & OT from Excel
    private static Map<String, List<WeeklyRecord>> readWeeklyHours(String filePath) {
        Map<String, List<WeeklyRecord>> weeklyHours = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String empID = getCellValue(row.getCell(0));
                String weekStart = getCellValue(row.getCell(1));
                double hoursWorked = getNumericCellValue(row.getCell(2));
                double otHours = getNumericCellValue(row.getCell(3));  // ✅ Read OT Hours

                weeklyHours.computeIfAbsent(empID, k -> new ArrayList<>())
                           .add(new WeeklyRecord(weekStart, hoursWorked, otHours));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weeklyHours;
    }

    // Handle String and Numeric Cell Values
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";    
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
    }

    private static double getNumericCellValue(Cell cell) {
        if (cell == null) return 0.0;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    yield 0.0;
                }
            }
            default -> 0.0;
        };
    }

    // Employee Class
    static class Employee {
        String employeeID, lastName, firstName;
        double hourlyRate, governmentDeductions, weeklyGrossDeduction;
        double riceAllowance, phoneAllowance, clothingAllowance;

        public Employee(String employeeID, String lastName, String firstName, double hourlyRate, double governmentDeductions,
                        double weeklyGrossDeduction, double riceAllowance, double phoneAllowance, double clothingAllowance) {
            this.employeeID = employeeID;
            this.lastName = lastName;
            this.firstName = firstName;
            this.hourlyRate = hourlyRate;
            this.governmentDeductions = governmentDeductions;
            this.weeklyGrossDeduction = weeklyGrossDeduction;
            this.riceAllowance = riceAllowance;
            this.phoneAllowance = phoneAllowance;
            this.clothingAllowance = clothingAllowance;
        }
    }

    // Weekly Record Class
    static class WeeklyRecord {
        String weekStart;
        double hoursWorked, otHours;

        public WeeklyRecord(String weekStart, double hoursWorked, double otHours) {
            this.weekStart = weekStart;
            this.hoursWorked = hoursWorked;
            this.otHours = otHours;
        }
    }
}
