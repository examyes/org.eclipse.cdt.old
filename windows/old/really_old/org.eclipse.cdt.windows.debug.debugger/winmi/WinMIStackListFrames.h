#ifndef WINMISTACKLISTFRAMES_H_
#define WINMISTACKLISTFRAMES_H_

#include <windows.h>
#include <MICommand.h>
#include <WinDebugCommand.h>

struct WinDebugFrame;

class WinMIStackListFrames : public MICommand, public WinDebugCommand
{
public:
	WinMIStackListFrames(MIEngine & miEngine, string & token);
	virtual ~WinMIStackListFrames();
	
	// MICommand
	virtual void addParameter(string & parameter);
	virtual void runCommand();
	virtual void sendResult(ostream & out);
	
	// WinDebugCommand
	virtual void execute(WinDebugEngine & debugEngine);

private:
	enum { param0, param1, param2 } paramState;
	ULONG depth;
	WinDebugFrame * frames;
	
	bool error;
	string msg;
};

#endif /*WINMISTACKLISTFRAMES_H_*/
