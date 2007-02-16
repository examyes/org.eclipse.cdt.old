#ifndef WINMIEXECRUN_H_
#define WINMIEXECRUN_H_

#include <MICommand.h>
#include "../win/WinDebugRunCommand.h"
#include <windows.h>

#include <list>
using namespace std;

// Also used for exec-continue

class WinMIExecRun : public MICommand, public WinDebugRunCommand
{
public:
	WinMIExecRun(MIEngine & miEngine, string & token)
		: MICommand(miEngine, token), status(_none) { }
	virtual ~WinMIExecRun() { }
	
	// MICommand
	virtual void runCommand();
	virtual void sendResult(ostream & out);
	
	// WinDebugCommand
	virtual void execute(WinDebugEngine & debugEngine);
	virtual void stopped(WinDebugEngine & debugEngine);
	
private:
	enum { _none, _running, _stopped, _error } status;
	string msg;
};

#endif /*WINMIEXECRUN_H_*/
