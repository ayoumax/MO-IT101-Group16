/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package test.weeklydeductions;

/**
 *
 * @author Ayou
 */
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

class WeeklyDeductions {
    static class Employee {
        String employeeId, lastName, firstName;
        double monthlyRate, hourlyRate;
        Map<YearMonth, TreeMap<LocalDate, Double>> weeklyHours = new HashMap<>();
    }

    public static void main(String[] args) {
        // ✅ Get absolute path of the project directory
        String projectPath = Paths.get("").toAbsolutePath().toString();
        String employeeFile = projectPath + "/employee_salaries.xlsx";  
        String hoursFile = projectPath + "/weekly_hours.xlsx";  

        System.out.println("🔍 Looking for weekly hours file at: " + hoursFile);

        List<Employee> employees = readEmployeeData(employeeFile);
        readWeeklyHours(employees, hoursFile);
        displayDeductions(employees);
    }

    // 1. Read Employee Salary Data
    private static List<Employee> readEmployeeData(String filePath) {
        List<Employee> employees = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                Employee emp = new Employee();
                
                Cell empCell = row.getCell(0);
                emp.employeeId = (empCell.getCellType() == CellType.NUMERIC) ? 
                                String.valueOf((int) empCell.getNumericCellValue()) : empCell.getStringCellValue().trim();

                emp.lastName = row.getCell(1).getStringCellValue().trim();
                emp.firstName = row.getCell(2).getStringCellValue().trim();
                emp.monthlyRate = row.getCell(3).getNumericCellValue();
                emp.hourlyRate = row.getCell(4).getNumericCellValue();

                employees.add(emp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return employees;
    }

    // 2. Read Weekly Hours
    private static void readWeeklyHours(List<Employee> employees, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("❌ ERROR: weekly_hours.xlsx not found at: " + filePath);
            System.err.println("💡 Run `EmployeeWorkHours.java` first to generate the file.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String empId;
                Cell empCell = row.getCell(0);
                empId = (empCell.getCellType() == CellType.NUMERIC) ? 
                        String.valueOf((int) empCell.getNumericCellValue()) : empCell.getStringCellValue().trim();

                LocalDate weekStart;
                Cell dateCell = row.getCell(1);
                if (dateCell.getCellType() == CellType.NUMERIC) {
                    weekStart = dateCell.getLocalDateTimeCellValue().toLocalDate();
                } else {
                    weekStart = LocalDate.parse(dateCell.getStringCellValue().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }

                double weeklyHours;
                Cell hoursCell = row.getCell(2);
                if (hoursCell.getCellType() == CellType.NUMERIC) {
                    weeklyHours = hoursCell.getNumericCellValue();
                } else {
                    weeklyHours = Double.parseDouble(hoursCell.getStringCellValue().trim());
                }

                for (Employee emp : employees) {
                    if (emp.employeeId.equals(empId)) {
                        YearMonth yearMonth = YearMonth.from(weekStart);
                        emp.weeklyHours.putIfAbsent(yearMonth, new TreeMap<>());
                        emp.weeklyHours.get(yearMonth).put(weekStart, weeklyHours);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 3. Display Weekly Deductions (Table aligned & centered)
    private static void displayDeductions(List<Employee> employees) {
        for (Employee emp : employees) {
            System.out.println("\nEmployee ID: " + emp.employeeId);
            System.out.println("Employee Name: " + emp.firstName + " " + emp.lastName);
            System.out.println("=======================================================================================================================================");
            System.out.printf("| %-10s | %-12s | %-10s | %-10s | %-10s | %-18s | %-18s| %-20s |\n",
                "Month", "Week Start", "SSS", "Pagibig", "Philhealth", "Total Govt Deductions", "Withholding Tax", "Weekly Gross Deduction");
            System.out.println("=======================================================================================================================================");

            for (var monthEntry : emp.weeklyHours.entrySet()) {
                YearMonth yearMonth = monthEntry.getKey();
                boolean firstRow = true;

                for (var weekEntry : monthEntry.getValue().entrySet()) {
                    LocalDate weekStart = weekEntry.getKey();
                    double weeklyHours = weekEntry.getValue();
                    double sss = computeSSS(emp.monthlyRate);
                    double pagibig = computePagibig(emp.monthlyRate);
                    double philhealth = computePhilHealth(emp.monthlyRate);
                    double withholdingTax = computeWithholdingTax(emp.monthlyRate);
                    
                    double totalGovtDeductions = sss + pagibig + philhealth;
                    double weeklyGrossDeductions = totalGovtDeductions + withholdingTax;

                    System.out.printf("| %-10s | %-12s | %10.2f | %10.2f | %10.2f | %18.2f | %18.2f |   %20.2f |\n",
                        (firstRow ? yearMonth.getMonth() : ""), weekStart, sss, pagibig, philhealth, totalGovtDeductions, withholdingTax, weeklyGrossDeductions);
                    firstRow = false;
                }
            }
            System.out.println("=======================================================================================================================================");
        }
    }

    // 4. Compute Deductions
    private static double computeSSS(double monthlyRate) {
    double[][] sssTable = {
        {0, 3250, 135}, {3250, 3750, 157.5}, {3750, 4250, 180}, {4250, 4750, 202.5},
        {4750, 5250, 225}, {5250, 5750, 247.5}, {5750, 6250, 270}, {6250, 6750, 292.5},
        {6750, 7250, 315}, {7250, 7750, 337.5}, {7750, 8250, 360}, {8250, 8750, 382.5},
        {8750, 9250, 405}, {9250, 9750, 427.5}, {9750, 10250, 450}, {10250, 10750, 472.5},
        {10750, 11250, 495}, {11250, 11750, 517.5}, {11750, 12250, 540}, {12250, 12750, 562.5},
        {12750, 13250, 585}, {13250, 13750, 607.5}, {13750, 14250, 630}, {14250, 14750, 652.5},
        {14750, 15250, 675}, {15250, 15750, 697.5}, {15750, 16250, 720}, {16250, 16750, 742.5},
        {16750, 17250, 765}, {17250, 17750, 787.5}, {17750, 18250, 810}, {18250, 18750, 832.5},
        {18750, 19250, 855}, {19250, 19750, 877.5}, {19750, 20250, 900}, {20250, 20750, 922.5},
        {20750, 21250, 945}, {21250, 21750, 967.5}, {21750, 22250, 990}, {22250, 22750, 1012.5},
        {22750, 23250, 1035}, {23250, 23750, 1057.5}, {23750, 24250, 1080}, {24250, 24750, 1102.5},
        {24750, Double.MAX_VALUE, 1125} // Maximum contribution
    };

    for (double[] bracket : sssTable) {
        if (monthlyRate >= bracket[0] && monthlyRate <= bracket[1]) {
            return bracket[2] / 4;  // ✅ Divide the contribution by 4 for weekly deduction
        }
    }
    return 1125 / 4;  // Default to max if above range
}

    private static double computePhilHealth(double monthlyRate) {
        double monthlyPremium = Math.min(Math.max(monthlyRate * 0.03, 300), 1800);
        return (monthlyPremium / 2) / 4;
    }

    private static double computePagibig(double monthlyRate) {
        return Math.min(monthlyRate * 0.02, 100) / 4;
    }

    private static double computeWithholdingTax(double monthlyRate) {
        if (monthlyRate <= 20832) return 0;
        else if (monthlyRate <= 33333) return ((monthlyRate - 20833) * 0.2) / 4;
        else if (monthlyRate <= 66667) return ((2500 + (monthlyRate - 33333) * 0.25)) / 4;
        else return ((10833 + (monthlyRate - 66667) * 0.3)) / 4;
    }
}
