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
	
private:
	char * command;
	
	IDebugClient * debugClient;
	IDebugControl * debugControl;
	
	list<WinDebugCommand *> commandQueue;
	HANDLE commandMutex;
	HANDLE commandReadyEvent;
	
	WinDebugRunCommand * currentRunCommand;
};

#endif /*WINDEBUGENGINE_H_*/
