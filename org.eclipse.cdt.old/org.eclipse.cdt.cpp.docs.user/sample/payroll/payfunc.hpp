//************************************************
//*** payfunc.hpp -- functions for payroll.cpp ***
//************************************************

//
// Member functions for the manager class
//


// Constructor definition for the employee class
employee::employee(char * n, int id)
{
  name = n;               // Initializing
  employee_id = id;
};

//
// Member functions for the manager class
//

// Constructor definition for the manager class
// Note the different way to initialize
manager::manager(char *n, int id, double sal)
  : employee(n, id), salary(sal)
{
}

//
// Definition of the pay function for the manager class
//
double manager::pay()
{
  return salary / 12 ;
}

//
// Definition of the print function for the manager class
//
void manager::print()
{
  cout << name << " with employee number ";
  cout << employee_id << " makes " << salary;
  cout << " per year." << endl;
  cout << name << " made " << pay();
  cout << " dollars this month" << endl;
  cout << endl;
}

//
// Member functions for the regular_emp class
//

// Constructor definition for the regular_emp class
regular_emp::regular_emp(char *n, int id, double wg, double hrs)
  : employee(n, id)
{
  wage = wg;
  hours = hrs;
}

// Definition of pay function for the regular_emp class
double regular_emp::pay()
{
  return wage * hours;
}

// Definition of the print function for the regular_emp class
void regular_emp::print()
{
  cout << name << " with employee number ";
  cout << employee_id << " makes " << wage;
  cout << " dollars per hour" << endl;
  cout << " and worked " << hours << " hours this month." << endl;
  cout << name <<" made " << pay();
  cout << " dollars this month." << endl;
  cout << endl;
}

//
// Member functions for the sales_person class
//

// Constructor definition for the sales_person class
sales_person::sales_person(char *n, int id, double comm, double nts)
  : employee(n, id), commission(comm), units(nts) {}

// Definition of pay function for the sales_person class
double sales_person::pay()
{
         return (commission * units);
}

// Definition of the print function for the sales_person class
void sales_person::print()
{
  cout << name << " with employee number ";
  cout << employee_id << " works for a straight commission of ";
  cout << commission << " dollars per unit sold and sold ";
  cout << units << " units this month." << endl;
  cout << name <<" made " << pay();
  cout << " dollars this month." << endl;
  cout << endl;
}

//
// Member functions for the sales_mgr class
//

// Constructor definition for the sales_mgr class
sales_mgr::sales_mgr(char *n, int id, double sal, double comm, double nts)
  : manager(n, id, sal), sales_person(n, id, comm, nts)
{
  name = n;
  employee_id = id;
  salary = sal;
  commission = comm;
  units = nts;
}

//   Definition of pay function for the sales_mgr class
double sales_mgr::pay()
{
  return (manager::pay() + sales_person::pay());
}

// Definition of the print function for the sales_mgr class
void sales_mgr::print()
{
  cout << name << " with employee number ";
  cout << employee_id << " makes " << salary;
  cout << " per year and earns a commission of ";
  cout << commission << " dollars per unit sold." << endl;
  cout << name << " was responsible for sales of ";
  cout << units << " units this month." << endl;
  cout << name <<" made " << pay();
  cout << " dollars this month." << endl;
  cout << endl;
}

// End of payfunc.hpp (member function definitions)
