#ifndef WINDEBUGENGINE_H_
#define WINDEBUGENGINE_H_

class WinDebugCommand;

class WinDebugEngine
{
public:
	WinDebugEngine();
	virtual ~WinDebugEngine();
	
	void enqueueCommand(WinDebugCommand * command);
	void mainLoop();
};

#endif /*WINDEBUGENGINE_H_*/
