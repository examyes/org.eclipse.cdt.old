#ifndef WINDEBUGENGINE_H_
#define WINDEBUGENGINE_H_

#include <windows.h>
#include <dbgeng.h>
#include <list>
using namespace std;

class WinDebugCommand;
class WinDebugRunCommand;

class WinDebugEngine
{
public:
	WinDebugEngine(char * _command);
	~WinDebugEngine() { }
	
	void enqueueCommand(WinDebugCommand * command);
	void mainLoop();

	void run(WinDebugRunCommand * runCommand);
	
	IDebugClient * getDebugClient() { return debugClient; }
	IDebugControl * getDebugControl() { return debugControl; }
	IDebugSymbols * getDebugSymbols() { return debugSymbols; }
	
private:
	char * command;
	
	IDebugClient * debugClient;
	IDebugControl * debugControl;
	IDebugSymbols * debugSymbols;
	
	list<WinDebugCommand *> commandQueue;
	HANDLE commandMutex;
	HANDLE commandReadyEvent;
	
	WinDebugRunCommand * currentRunCommand;
};

#endif /*WINDEBUGENGINE_H_*/
