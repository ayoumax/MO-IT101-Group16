/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package test.employeeworkhours;

/**
 * Employee Work Hours Processor.
 * Reads login/logout data from an Excel file.
 * Computes weekly work hours and overtime.
 * Generates and exports a report.
 * 
 * @author Group16;Arriesgado.M,Marquez.N,Lozada.K,Aquino.N
 *
 */
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EmployeeWorkHours {
    static class WorkLog {
        String employeeId, lastName, firstName;
        LocalDate date;
        LocalTime login, logout;
    }
     /**
     * Entry point to run Employee Work Hours processing.
     */
    public void run() { 
        System.out.println("📊 Processing Employee Work Hours...");
        main(null); 
    }
    
    public static void main(String[] args) {
        String filePath = "employee_logs.xlsx";  // Input file
        List<WorkLog> logs = readExcel(filePath);
        Map<String, Map<LocalDate, Double[]>> weeklyHours = processWorkLogs(logs);

        displayReport(weeklyHours, logs);
        exportToExcel(weeklyHours); //
    }    
     /**
     * Reads login/logout data from an Excel file.
     */   
    private static List<WorkLog> readExcel(String filePath) {
        List<WorkLog> logs = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("h:mm:ss a");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                WorkLog log = new WorkLog();
                Cell empCell = row.getCell(0);
                log.employeeId = (empCell.getCellType() == CellType.NUMERIC) ? 
                                String.valueOf((int) empCell.getNumericCellValue()) : empCell.getStringCellValue().trim();

                log.lastName = row.getCell(1).getStringCellValue().trim();
                log.firstName = row.getCell(2).getStringCellValue().trim();

                // Date Handling
                Cell dateCell = row.getCell(3);
                log.date = (dateCell.getCellType() == CellType.NUMERIC) ? 
                           dateCell.getLocalDateTimeCellValue().toLocalDate() :
                           LocalDate.parse(dateCell.getStringCellValue().trim(), DateTimeFormatter.ofPattern("M/d/yyyy"));

                // Login & Logout Time Handling
                Cell loginCell = row.getCell(4);
                log.login = (loginCell.getCellType() == CellType.STRING) ?
                            LocalTime.parse(loginCell.getStringCellValue().trim(), timeFormat) :
                            loginCell.getLocalDateTimeCellValue().toLocalTime();

                Cell logoutCell = row.getCell(5);
                log.logout = (logoutCell.getCellType() == CellType.STRING) ?
                             LocalTime.parse(logoutCell.getStringCellValue().trim(), timeFormat) :
                             logoutCell.getLocalDateTimeCellValue().toLocalTime();

                logs.add(log);
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR: Failed to read Excel file: " + filePath);
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Processes work logs and computes weekly hours.
     */
    private static Map<String, Map<LocalDate, Double[]>> processWorkLogs(List<WorkLog> logs) {
        Map<String, Map<LocalDate, Double[]>> weeklyHours = new HashMap<>();

        for (WorkLog log : logs) {
            LocalDate weekStart = log.date.with(DayOfWeek.MONDAY); //
            LocalTime workStart = LocalTime.of(8, 0);
            LocalTime workEnd = LocalTime.of(17, 0);
            LocalTime gracePeriod = LocalTime.of(8, 10); // 10 min grace period

            // Adjust Login & Logout Time
            LocalTime actualLogin = log.login.isBefore(workStart) ? workStart : log.login;
            LocalTime actualLogout = log.logout.isAfter(workEnd) ? workEnd : log.logout;

            // Compute hours within working hours (excluding 1 hr lunch break)
            double workHours = Duration.between(actualLogin, actualLogout).toMinutes() / 60.0;
            workHours = Math.max(workHours - 1, 0); // Deduct 1 hour for lunch

            // Compute Overtime (Only if NOT late)
            double overtime = 0;
            if (log.login.isBefore(gracePeriod)) { 
                overtime = Math.max(0, Duration.between(workEnd, log.logout).toMinutes() / 60.0);
            }
            // Store weekly data
            weeklyHours.putIfAbsent(log.employeeId, new TreeMap<>());
            Double[] hours = weeklyHours.get(log.employeeId).getOrDefault(weekStart, new Double[]{0.0, 0.0});
            hours[0] += workHours;
            hours[1] += overtime;
            weeklyHours.get(log.employeeId).put(weekStart, hours);
        }
        return weeklyHours;
    }

    // * Display Weekly Hours Report (Formatted Table)
    private static void displayReport(Map<String, Map<LocalDate, Double[]>> weeklyHours, List<WorkLog> logs) {
        Map<String, String[]> employeeDetails = new HashMap<>();
        for (WorkLog log : logs) {
            employeeDetails.putIfAbsent(log.employeeId, new String[]{log.lastName, log.firstName});
        }
        for (var entry : weeklyHours.entrySet()) {
            String empId = entry.getKey();
            String lastName = employeeDetails.get(empId)[0];
            String firstName = employeeDetails.get(empId)[1];

            System.out.println("\nEmployee ID  : " + empId);
            System.out.println("Employee Name: " + firstName + " " + lastName);
            System.out.println("======================================================");
            System.out.printf(" %-10s  %-12s  %-15s  %-10s \n", "Month", "Week Start", "Weekly Hours", "OT Hours");
            System.out.println("======================================================");

            // Sort and group months properly
            TreeMap<YearMonth, TreeMap<LocalDate, Double[]>> sortedData = new TreeMap<>();
            for (var weekEntry : entry.getValue().entrySet()) {
                YearMonth month = YearMonth.from(weekEntry.getKey());
                sortedData.putIfAbsent(month, new TreeMap<>());
                sortedData.get(month).put(weekEntry.getKey(), weekEntry.getValue());
            }

            for (var monthEntry : sortedData.entrySet()) {
                YearMonth yearMonth = monthEntry.getKey();
                boolean firstRow = true;

                for (var weekEntry : monthEntry.getValue().entrySet()) {
                    LocalDate weekStart = weekEntry.getKey();
                    double hours = weekEntry.getValue()[0];
                    double otHours = weekEntry.getValue()[1];

                    System.out.printf(" %-10s  %-12s  %-15.2f  %-10.2f \n",
                        (firstRow ? yearMonth.getMonth() : ""), weekStart, hours, otHours);
                    firstRow = false;
                }
            }
            System.out.println("======================================================");
        }
    }

    //* Export Weekly Hours to Excel
    private static void exportToExcel(Map<String, Map<LocalDate, Double[]>> weeklyHours) {
    String filePath = "weekly_hours.xlsx";

    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Weekly Hours");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Employee ID");
        headerRow.createCell(1).setCellValue("Week Start");
        headerRow.createCell(2).setCellValue("Weekly Hours");
        headerRow.createCell(3).setCellValue("OT Hours"); 

        int rowIndex = 1;

        for (var entry : weeklyHours.entrySet()) {
            String empId = entry.getKey();

            for (var weekEntry : entry.getValue().entrySet()) {
                LocalDate weekStart = weekEntry.getKey();
                double hoursWorked = weekEntry.getValue()[0];
                double otHours = weekEntry.getValue()[1];  //

                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(empId);
                row.createCell(1).setCellValue(weekStart.toString());
                row.createCell(2).setCellValue(hoursWorked);
                row.createCell(3).setCellValue(otHours);  //
            }
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }
        System.out.println("Weekly hours exported to: " + filePath);

    } catch (Exception e) {
        e.printStackTrace();
    }
}}


