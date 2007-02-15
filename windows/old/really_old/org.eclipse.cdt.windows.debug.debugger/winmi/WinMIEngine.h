#ifndef WINDEBUGHANDLER_H_
#define WINDEBUGHANDLER_H_

#include <MIEngine.h>

class WinDebugEngine;

class WinMIEngine : public MIEngine
{
public:
	WinMIEngine(WinDebugEngine & _debugEngine) : debugEngine(_debugEngine) { }
	virtual ~WinMIEngine() { }
	
protected:
	MICommand * createMICommand(string & token, string & operation);
	
private:
	WinDebugEngine & debugEngine;
};

#endif /*WINDEBUGHANDLER_H_*/
