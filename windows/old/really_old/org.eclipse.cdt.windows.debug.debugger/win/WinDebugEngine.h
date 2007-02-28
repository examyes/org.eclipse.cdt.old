#ifndef WINDEBUGENGINE_H_
#define WINDEBUGENGINE_H_

#include <windows.h>
#include <dbgeng.h>
#include <list>
using namespace std;

class WinDebugCommand;
class WinDebugRunCommand;
struct WinDebugFrame;

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
	
	HANDLE getProcess() { return process; }
	
	void processCreated(HANDLE process);
	
	ULONG getNumFrames() { return numFrames; }
	WinDebugFrame * getFrames() { return frames; }
	
private:
	char * command;
	
	IDebugClient * debugClient;
	IDebugControl * debugControl;
	
	HANDLE process;
	
	ULONG numFrames;
	WinDebugFrame * frames;
	
	bool populateFrames();
	
	list<WinDebugCommand *> commandQueue;
	HANDLE commandMutex;
	HANDLE commandReadyEvent;
	
	WinDebugRunCommand * currentRunCommand;
};

#endif /*WINDEBUGENGINE_H_*/
