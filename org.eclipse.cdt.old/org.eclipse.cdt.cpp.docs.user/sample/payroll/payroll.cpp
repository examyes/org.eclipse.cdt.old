/**************************************************************
*** payroll.cpp main source file for sample payroll program ***
**************************************************************/
#include <iostream.h>    // Include file for C++ input and output operations
#include "payclass.hpp"  // Include file for class definitions
#include "payfunc.hpp"   // Include file for function definitions

void payout(double);
void payout(double, double);
void payout(double, double, double);

inline void title() // Example of defining an inline function
{
  cout << "Monthly Employee Pay Report" << endl;
  cout << endl;
};

int main() // Start of the main function
{
  // Assigning values to variables. Use const so
  // a program could not inadvertantly change a value
  const double managers_pay = 1510.35;
  const double reg_emp_pay = 25.75;
  const double reg_emp_hrs = 40.00;
  const double sales_mgr_pay = 880.75;
  const double sales_mgr_commission = 1.15;
  const double sales_mgr_units = 200;
  const double monthly_salary = 800.00;
  const double commission = 1.00;
  const double units = 150;

  // Use the functions in cout to set the decimal places
  // in the output
  cout.setf(ios::fixed);
  cout.precision(2);

  // Use the inline function title
  title();

  // Use the payout function
  payout(managers_pay);

  // Use the overloaded payout function
  payout(reg_emp_pay, reg_emp_hrs);

  // Use another overloaded payout function
  payout(monthly_salary, commission, units);

  cout << "View data on employees: ";

  // Define an instance, smith, of the manager class
  manager smith("Jack Smith", 123, 28020);

  // Do position and print functions for this instance
  smith.position();
  smith.print();

  // Define an instance, james, of the regular_emp class
  regular_emp james("Everett James", 456, 12, 160);

  // Do position and print functions for this instance
  james.position();
  james.print();

  // Define an instance, doe, of the sales_person class
  sales_person doe("Jackson Doe", 101, 31, 65);

  // Do position and print functions for this instance
  doe.position();
  doe.print() ;

  // Define an instance, stevens, of the sales_mgr class
  sales_mgr stevens("Jennifer Stevens", 789, 28000, 4, 105) ;

  // Do position and print functions for this instance
  stevens.position();
  stevens.print();

  // Declaring variables where they are to be used
  double sal1, sal2, sal3, sal4;

  sal1 = smith.pay();
  sal2 = james.pay();
  sal3 = doe.pay();
  sal4 = stevens.pay();

  // Use the values returned from the pay functions
  cout << "Total wages paid this month were: ";
  cout << (sal1 + sal2 + sal3 + sal4);
  cout << " dollars" << endl;
  cout << endl;

  return 0;
};
// End of main function

// Definition of payout functions (declared as prototypes before main)

// Example of an overloaded function.

// One argument
void payout(double managers_pay)
{
  cout << "The basic salary for a manager is: ";
  cout << managers_pay << " dollars per month." << endl;
  cout << endl;
};

// Two arguments
void payout(double reg_emp_pay, double reg_emp_hrs)
{
  double reg_monthly_pay;
  reg_monthly_pay = reg_emp_pay * reg_emp_hrs;

  cout << "The basic pay for a regular employee is ";
  cout << reg_monthly_pay  << " dollars per month." << endl;
  cout << endl;
};

// Three arguments
void payout(double monthly_salary, double commission, double units)
{
  double reg_monthly_pay;
  reg_monthly_pay = (monthly_salary + (commission * units));

  cout << "The basic pay for a sales manager is ";
  cout << reg_monthly_pay  << " dollars per month." << endl;
  cout << endl;
};

// End of payroll.cpp
