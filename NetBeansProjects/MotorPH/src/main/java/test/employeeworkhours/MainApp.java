/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.employeeworkhours;

/**
 * Main Application to execute Employee Work Hours, Weekly Deductions, and Payroll Calculations.
 * Ensures all modules are executed in sequence.
 * 
 * @author Group16;Arriesgado.M,Marquez.N,Lozada.K,Aquino.N
 */
public class MainApp {
    public static void main(String[] args) {
        System.out.println("🚀 Starting MotorPHPayroll System...");

        try {
            // * Run Employee Work Hours Calculation
            EmployeeWorkHours employeeWorkHours = new EmployeeWorkHours();
            employeeWorkHours.run();  

            // * Run Weekly Deductions
            WeeklyDeductions weeklyDeductions = new WeeklyDeductions();
            weeklyDeductions.run();  

            // * Run Payroll Calculation
            PayrollCalculator payrollCalc = new PayrollCalculator();  
            payrollCalc.run(); 

            System.out.println("✅ Payroll processing completed!");
        } catch (Exception e) {
            System.err.println("❌ ERROR: An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
