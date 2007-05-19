#ifndef WINDEBUGENGINE_H_
#define WINDEBUGENGINE_H_

#include <windows.h>
#include <dbghelp.h>

#include <list>
#include <vector>
#include <map>
using namespace std;

class WinDebugCommand;
class WinDebugRunCommand;
class WinDebugBreakpoint;
struct WinDebugFrame;

class WinDebugEngine
{
public:
	WinDebugEngine(char * _command);
	~WinDebugEngine() { }
	
	void enqueueCommand(WinDebugCommand * command);
	void mainLoop();

	void run(WinDebugRunCommand * runCommand);
	
	HANDLE getProcess() { return process; }
	
	ULONG getNumFrames() { return numFrames; }
	WinDebugFrame * getFrames() { return frames; }
	
	void addBreakpoint(WinDebugBreakpoint * bp);
	
	// Debug message box out put
	static void message(const char * msg);
	
private:
	char * command;

	HANDLE process;
	
	ULONG numFrames;
	WinDebugFrame * frames;
	
	bool populateFrames();
	
	vector<WinDebugBreakpoint *> breakpoints;
	map<DWORD64, WinDebugBreakpoint *> breakpointMap;
	
	list<WinDebugCommand *> commandQueue;
	HANDLE commandMutex;
	HANDLE commandReadyEvent;
	
	WinDebugRunCommand * currentRunCommand;
	
	DWORD handleEvent(DEBUG_EVENT & event);
};

#endif /*WINDEBUGENGINE_H_*/
