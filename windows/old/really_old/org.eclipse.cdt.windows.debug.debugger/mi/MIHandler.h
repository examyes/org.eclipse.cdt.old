#ifndef MIHANDLER_H_
#define MIHANDLER_H_

#include <iostream>
#include <string>
using namespace std;

class MICommand;
class MIExecAbort;

class MIHandler
{
private:
	istream & in;
	ostream & out;
	
public:
	MIHandler()	: in(cin), out(cout) {
	}
	
	// Calls the sendResult on the command when the output stream is ready
	void enqueueResult(MICommand * command);

	void inputLoop();

protected:
	// Override to add extended commands
	virtual MICommand * createCommand(string & operation);

	// The standard commands
	virtual MIExecAbort * createExecAbort() = 0;
	
private:
	bool parseCommand();
	void skipLine();
	int parseToken();
	bool parseOperation(string & operation);
	void reportUnsupportedCommand();
};

#endif /*MIHANDLER_H_*/
