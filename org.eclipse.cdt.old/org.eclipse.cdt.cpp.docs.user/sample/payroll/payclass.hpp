//*********************************************************
//*** payclass.hpp -- class definitions for payroll.cpp ***
//*********************************************************

//
// Abstract base class definition
//
class employee          // Declare a virtual base
                        // class named employee
{
protected:
  char * name;
  int employee_id;

public:
  // Constructors for employee class
  employee() { /* required for multiple inheritance of virtual base class */; }
  employee(char * n, int id);

  // Declare pure virtual functions
  virtual void print() = 0;
  virtual void position() = 0;
  virtual double pay() = 0;

}; // End of the employee class definition


// Derived class definitions

// Declare a class named manager which is
// derived from the employee class

class manager: virtual public employee
{
private:
  double salary;       // Declare a variable of type double
                       // private to this class
public:
  // Constructors for the manager class
  manager() { /* required for multiple inheritance of virtual base class */; }
  manager(char *n, int id, double sal);

  // pay function for the manager class
  double pay();

  // print function for the manager class
  void print();

  // position function for the manager class
  void position() // Defined inline function within the class definition
  {
    cout << name << " is a manager." << endl;
  }

}; // End of the manager class definition

// Another derived class definition

// Declare a class named regular_emp which is
// derived from the employee class
class regular_emp : virtual public employee
{
private:
  double wage, hours;   // Declare two variables of type
                        // double, private to this class
public:
  // Constructor for the regular_emp class
  regular_emp(char *n, int id, double wg, double hrs) ;

  // pay function for the regular_emp class
  double pay();

  // print function for the regular_emp class
  void print();

  // position function for the regular_emp class
  void position() // Defined inline function within the class definition.
  {
    cout << name << " is a regular employee." << endl;
  };

}; // End of the regular_emp class definition

// Another derived class definition

// Declare a class named sales_person which is
// derived from the employee class
class sales_person : virtual public employee
{
private:
  double commission;
  int units;

public:

  // Constructors for the sales_person class
  sales_person() { /* required for multiple inheritance of virtual base class */; }
  sales_person(char *n, int id, double com, double nts);

  // pay function for the sales_person class
  double pay();

  // print function for the sales_person class
  void print();

  // position function for the sales_person class
  void position() // Defined inline function within the class definition
  {
    cout << name << " is a sales person." << endl;
  };

}; // End of the sales_person class definition


// A definition of a class with multiple inheritance
// (inherits functions from manager and sales_person)

// Declare a class named sales_mgr which is
// derived from the manager and sales_person classes

class sales_mgr : public manager, public sales_person
{
private:
  double salary, commission;
  int units;

public:
  // Constructor for the sales_mgr class
  sales_mgr(char *n, int id, double sal, double comm, double nts);

  // pay function for the sales_mgr class
  double pay();

  // print function for the sales_mgr class
  void print();

  // position function for the sales_mgr class
  void position() // Defined inline function within the class definition
  {
    cout << name << " is a sales manager." << endl;
  };

}; // End of the sales_mgr class definition

// End of payclass.hpp (class definitions)
