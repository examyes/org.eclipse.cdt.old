#ifndef WINMISTACKINFODEPTH_H_
#define WINMISTACKINFODEPTH_H_

#include <MICommand.h>
#include <WinDebugCommand.h>
#include <windows.h>

class WinMIStackInfoDepth : public MICommand, public WinDebugCommand 
{
public:
	WinMIStackInfoDepth(MIEngine & miEngine, string & token);
	virtual ~WinMIStackInfoDepth();
	
	// MICommand
	virtual void runCommand();
	virtual void sendResult(ostream & out);
	
	// WinDebugCommand
	virtual void execute(WinDebugEngine & debugEngine);

private:
	ULONG depth;
	bool error;
	string msg;
};

#endif /*WINMISTACKINFODEPTH_H_*/
