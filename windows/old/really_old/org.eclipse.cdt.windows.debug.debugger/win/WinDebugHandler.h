#ifndef WINDEBUGHANDLER_H_
#define WINDEBUGHANDLER_H_

#include <MIHandler.h>

class WinDebugEngine;

class WinDebugHandler : public MIHandler
{
private:
	WinDebugEngine & engine;
	
public:
	WinDebugHandler(WinDebugEngine & _engine) : engine(_engine) { }
	virtual ~WinDebugHandler() { }
	
protected:
	MICommand * createCommand(string & operation);
	
	MIExecAbort * createExecAbort();
};

#endif /*WINDEBUGHANDLER_H_*/
