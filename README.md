**MotorPH Payroll System

About the Project

The MotorPH Payroll System is a Java-based payroll processing application designed to compute weekly work hours, overtime, deductions, and net wages for employees. It efficiently reads data from Excel files, performs calculations, and exports payroll results back to Excel for easy reporting.

****Key Features

✅ Work Hours & Overtime Computation

Reads employee login/logout times from login_logout.xlsx
Computes weekly work hours (8AM - 5PM, minus a 1-hour lunch)
Applies a 10-minute grace period for late arrivals
Excludes overtime for employees who arrived late

✅ Payroll Computation

Calculates weekly gross pay (Hourly Rate × Weekly Hours Worked)
Computes taxable pay (Gross Pay - Government Deductions)
Applies mandatory government deductions
SSS based on salary brackets
PhilHealth at 3% (Employer-Employee Shared)
Pag-IBIG at 2% (Max ₱100)
Calculates withholding tax using the Philippine tax brackets

✅ Automated Report Generation

Exports weekly work hours and payroll data to weekly_hours.xlsx
Logs errors and payroll process details into payroll.log for tracking

✅ Code Quality Improvements (Applied QA Feedback)

Improved comments and function headers for readability
Refactored redundant calculations into helper functions
Optimized variable usage to reduce recalculations
Implemented logging for debugging and tracking
Enhanced error handling for missing/invalid Excel files

****MotorPH_Payroll/

│── src/

│   ├── test/employeeworkhours/
│   │   ├── EmployeeWorkHours.java  # Processes work hours & overtime
│   │   ├── WeeklyDeductions.java   # Computes deductions (SSS, Pag-IBIG, PhilHealth)
│   │   ├── PayrollCalculator.java  # Computes gross pay, net pay, and tax
│   │   ├── MainApp.java            # Main entry point for execution
│── data/

│   ├── login_logout.xlsx           # Employee login/logout records
│   ├── weekly_hours.xlsx           # Computed weekly hours and OT
│   ├── employee_salaries.xlsx      # Employee salary details
│── logs/

│   ├── payroll.log                 # Log file for process tracking

│── README.md                       # Project documentation


**Getting Started

1. Installing Requirements
Install Java 23 (or latest version)
Install Apache POI (for Excel processing)
Install NetBeans (or any Java IDE)

2. Clone the Repository
git clone https://github.com/ayoumax/MO-IT101-Group16/tree/master.git
cd MotorPH

3. Open the Project in NetBeans
Click "Open Project" and select the MotorPH folder
Ensure all .xlsx files exist inside the src/ folder

4. Run the Payroll System
Right-click MainApp.java → Click Run File

Sample Output (Console)

🚀 Starting MotorPHPayroll System...
✅ Processing Employee Work Hours...
✅ Weekly hours exported to: weekly_hours.xlsx
✅ Running Weekly Deductions...
✅ Looking for weekly hours file at: weekly_hours.xlsx
✅ Deductions computed successfully.
✅ Calculating Payroll...
✅ Payroll processing completed!
✅ All payroll data saved to: weekly_hours.xlsx
✅ Logs saved to: payroll.log
✅ Contributors

****MO-IT101 S1101 - Group 16

Arriesgado, Macky
Marquez, Neil Kennedy
Lozada, Ma Kristel
Aquino, Nikki Mae

