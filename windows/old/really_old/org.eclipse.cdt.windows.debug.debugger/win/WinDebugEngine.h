#ifndef WINDEBUGENGINE_H_
#define WINDEBUGENGINE_H_

#include <dbgeng.h>

class WinDebugCommand;

class WinDebugEngine
{
private:
	char * command;
	IDebugClient * debugClient;
	IDebugControl * debugControl;
	
public:
	WinDebugEngine(char * _command) : command(_command) { }
	~WinDebugEngine() { }
	
	void enqueueCommand(WinDebugCommand * command);
	void mainLoop();
};

#endif /*WINDEBUGENGINE_H_*/
