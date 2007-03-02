#ifndef WINDEBUGENGINE_H_
#define WINDEBUGENGINE_H_

#include <dbgeng.h>
#include <dbghelp.h>

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
	
	// Calls into dbghelp to facilitate dynamic loading of dll
	typedef DWORD (__stdcall * SymSetOptionsProc)(DWORD SymOptions);
	SymSetOptionsProc symSetOptions;

	typedef BOOL (__stdcall * SymInitializeProc)(HANDLE hProcess,
	    PCSTR UserSearchPath, BOOL fInvadeProcess);
	SymInitializeProc symInitialize;
	
	typedef BOOL (__stdcall * SymFromAddrProc)(HANDLE hProcess,
	    DWORD64 Address, PDWORD64 Displacement, PSYMBOL_INFO Symbol);
	SymFromAddrProc symFromAddr;
	
	typedef BOOL (__stdcall * SymGetLineFromAddr64Proc)(HANDLE hProcess,
	    DWORD64 qwAddr, PDWORD pdwDisplacement, PIMAGEHLP_LINE64 Line64);
	SymGetLineFromAddr64Proc symGetLineFromAddr64;

	// Debug message box out put
	static void message(const char * msg);
	
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
