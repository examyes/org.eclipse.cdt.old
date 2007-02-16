#ifndef MIHANDLER_H_
#define MIHANDLER_H_

#include <windows.h>

#include <iostream>
#include <string>
using namespace std;

class MICommand;
class MIExecAbort;

class MIEngine
{
private:
	istream & in;
	ostream & out;
	
public:
	MIEngine();
	
	// Calls the sendResult on the command when the output stream is ready
	void enqueueResult(MICommand * command);

	void inputLoop();

protected:
	// Override to add extended commands
	virtual MICommand * createMICommand(string & token, string & operation);
	virtual MICommand * createCLICommand(string & token, string & operation); 

private:
	HANDLE outputMutex;
	
	MICommand * parseCommand();
	void parseToken(string & token);
	void parseParameter(string & parameter);
	void skipLine();
};

#endif /*MIHANDLER_H_*/
