#ifndef WINMIBREAKINSERT_H_
#define WINMIBREAKINSERT_H_

#include <MICommand.h>
#include "../win/WinDebugCommand.h"
#include <windows.h>

class IDebugBreakpoint;

class WinMIBreakInsert : public MICommand, public WinDebugCommand
{
public:
	WinMIBreakInsert(MIEngine & miEngine, string & token);
	virtual ~WinMIBreakInsert() { }
	
	// MICommand
	virtual void addParameter(string & parameter);
	virtual void runCommand();
	virtual void sendResult(ostream & out);
	
	// WinDebugCommand
	virtual void execute(WinDebugEngine & debugEngine);

private:
	string target;
	bool isTemporary;
	ULONG id;
	ULONG flags;
	ULONG64 offset;
	ULONG line;
	string file;
	
	void recordError(const char * _msg);
	bool error;
	string msg;
};

#endif /*WINMIBREAKINSERT_H_*/
