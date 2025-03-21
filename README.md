**MotorPH Payroll System
**
**About the Project
**The MotorPH Payroll System is a Java-based payroll processing application designed to compute weekly work hours, overtime, deductions, and net wages for employees. It efficiently reads data from Excel files, performs calculations, and exports payroll results back to Excel for easy reporting.

_Key Features
_✅ 1. Work Hours & Overtime Computation
Reads employee login/logout times from login_logout.xlsx
Computes weekly work hours (8 AM - 5 PM, minus a 1-hour lunch)
Applies a 10-minute grace period for late arrivals
Excludes overtime for employees who arrived late
✅ 2. Payroll Computation
Calculates weekly gross pay (Hourly Rate × Weekly Hours Worked)
Computes taxable pay (Gross Pay - Government Deductions)
Applies mandatory government deductions
SSS (Social Security) based on salary brackets
PhilHealth (Health Insurance) at 3% (Employer-Employee Shared)
Pag-IBIG (Housing Fund) at 2% (Max ₱100)
Calculates withholding tax using the Philippine tax brackets
✅ 3. Automated Report Generation
Exports weekly work hours and payroll data to weekly_hours.xlsx
Logs errors and payroll process details into payroll.log for tracking
✅ 4. Code Quality Improvements (Applied QA Feedback)
Improved comments and function headers for readability
Refactored redundant calculations into helper functions
Optimized variable usage to reduce recalculations
Implemented logging for debugging and tracking
Enhanced error handling for missing/invalid Excel files
