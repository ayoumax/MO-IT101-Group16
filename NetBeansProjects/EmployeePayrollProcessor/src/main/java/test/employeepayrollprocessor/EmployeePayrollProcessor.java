/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package test.employeepayrollprocessor;

/**
 *
 * @author Ayou
 */
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class EmployeePayrollProcessor {
    public static void main(String[] args) {
        try {
            // Open input file
            File inputFile = new File("employee_data.txt");
            Scanner scanner = new Scanner(inputFile);

            // Read the header row (contains week start dates)
            String header = scanner.nextLine();
            String[] columns = header.split(",");

            // Formatter for currency output
            DecimalFormat df = new DecimalFormat("#,##0.00");

            // Employee data storage
            Map<String, List<String>> monthlyReports = new LinkedHashMap<>();

            while (scanner.hasNextLine()) {
                String[] employeeData = scanner.nextLine().split(",");

                // Validate row length
                if (employeeData.length < columns.length) {
                    System.out.println("⚠ Warning: Skipping incomplete data for " + employeeData[0]);
                    continue;
                }

                // Extract employee details
                String employeeName = employeeData[0];
                double hourlyRate = Double.parseDouble(employeeData[1]);
                double monthlyBaseRate = Double.parseDouble(employeeData[2]);  // Now reading from file
                double riceAllowance = Double.parseDouble(employeeData[3]);
                double phoneAllowance = Double.parseDouble(employeeData[4]);
                double clothingAllowance = Double.parseDouble(employeeData[5]);

                // Display employee name before the table
                System.out.println("\n======================================================================================================================================");
                System.out.println("Employee: " + employeeName);
                System.out.println("======================================================================================================================================");
                System.out.printf("%-12s %-12s %-15s %-15s %-10s %-10s %-10s %-18s %-18s %-15s\n",
                        "Week Start", "Hours", "Gross Pay", "Taxable Pay", "SSS", "Pag-IBIG", "PhilHealth", "Withholding Tax", "Gross Deductions", "Net Pay");
                System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");

                // Loop through weekly hours for each date in the header
                for (int i = 6; i < columns.length; i++) {  // Now index 6 since Monthly Base Rate is included
                    String weekStart = columns[i];  // Correctly map week start from header
                    double hoursWorked = Double.parseDouble(employeeData[i]);  // Hours worked for that week

                    // Compute Weekly Gross Pay (No Allowances)
                    double weeklyGrossPay = (hourlyRate * hoursWorked);

                    // Compute Weekly Deductions (Divided by 4)
                    double sssDeduction = computeSSS(monthlyBaseRate) / 4;
                    double philhealthDeduction = computePhilHealth(monthlyBaseRate) / 4;
                    double pagibigDeduction = computePagibig(monthlyBaseRate) / 4;
                    double totalDeductions = sssDeduction + philhealthDeduction + pagibigDeduction;

                    // Compute Weekly Taxable Pay
                    double weeklyTaxablePay = weeklyGrossPay - totalDeductions;

                    // Compute Monthly Withholding Tax (NOT divided by 4 here)
                    double monthlyWithholdingTax = computeWithholdingTax(monthlyBaseRate);

                    // Compute Weekly Withholding Tax (Divide monthly result by 4)
                    double weeklyWithholdingTax = monthlyWithholdingTax / 4;

                    // Compute Weekly Gross Deduction
                    double weeklyGrossDeduction = totalDeductions + weeklyWithholdingTax;

                    // Compute Weekly Net Pay (Adding Allowances)
                    double netPay = weeklyGrossPay - weeklyGrossDeduction + riceAllowance + phoneAllowance + clothingAllowance;

                    // Display in table format
                    System.out.printf("%-12s %-12s %-15s %-15s %-10s %-10s %-10s %-18s %-18s %-15s\n",
                            weekStart, df.format(hoursWorked), df.format(weeklyGrossPay), df.format(weeklyTaxablePay),
                            df.format(sssDeduction), df.format(pagibigDeduction),
                            df.format(philhealthDeduction), df.format(weeklyWithholdingTax),
                            df.format(weeklyGrossDeduction), df.format(netPay));

                    // Store in monthly report
                    String reportLine = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                            weekStart, df.format(hoursWorked), df.format(weeklyGrossPay), df.format(weeklyTaxablePay),
                            df.format(sssDeduction), df.format(pagibigDeduction),
                            df.format(philhealthDeduction), df.format(weeklyWithholdingTax),
                            df.format(weeklyGrossDeduction), df.format(netPay));

                    monthlyReports.putIfAbsent(employeeName, new ArrayList<>());
                    monthlyReports.get(employeeName).add(reportLine);
                }

                System.out.println("======================================================================================================================================");
            }

            scanner.close();

            // Save results to output file
            saveToFile(monthlyReports);
            System.out.println("Payroll report successfully saved to payroll_report.txt!");

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // Compute SSS based on monthly base rate
    private static double computeSSS(double salary) {
        if (salary < 3250) return 135;
        if (salary > 24750) return 1125;
        return 135 + ((Math.floor((salary - 3250) / 500)) * 22.5);
    }

    // Compute PhilHealth (3% employee share, max 900/month)
    private static double computePhilHealth(double salary) {
        double monthlyPremium = salary * 0.03;
        return Math.min(monthlyPremium / 2, 900); // Employee share
    }

    // Compute Pag-IBIG (2% capped at 100)
    private static double computePagibig(double salary) {
        return Math.min(salary * 0.02, 100);
    }

    // Compute Withholding Tax based on Monthly Base Rate (No Division Here)
    private static double computeWithholdingTax(double monthlyBaseRate) {
        if (monthlyBaseRate <= 20832) return 0;
        if (monthlyBaseRate <= 33333) return (monthlyBaseRate - 20833) * 0.20;
        if (monthlyBaseRate <= 66667) return 2500 + (monthlyBaseRate - 33333) * 0.25;
        if (monthlyBaseRate <= 166667) return 10833 + (monthlyBaseRate - 66667) * 0.30;
        if (monthlyBaseRate <= 666667) return 40833.33 + (monthlyBaseRate - 166667) * 0.32;
        return 200833.33 + (monthlyBaseRate - 666667) * 0.35;
    }

    // Save output to file
    private static void saveToFile(Map<String, List<String>> reports) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("payroll_report.txt"));
        for (String employee : reports.keySet()) {
            writer.write("\n=== Payroll for " + employee + " ===\n");
            writer.write("Week Start,Hours Worked,Gross Pay,Taxable Pay,SSS,Pag-IBIG,PhilHealth,Withholding Tax,Gross Deductions,Net Pay\n");
            for (String report : reports.get(employee)) writer.write(report);
        }
        writer.close();
    }
}
