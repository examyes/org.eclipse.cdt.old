#ifndef WINMIEXECNEXT_H_
#define WINMIEXECNEXT_H_

#include "WinMIExecRun.h"
#include <windows.h>

class WinMIExecNext : public WinMIExecRun
{
public:
	WinMIExecNext(MIEngine & miEngine, string & token);
	virtual ~WinMIExecNext();
	
	virtual void execute(WinDebugEngine & debugEngine);

protected:
	virtual ULONG getExecutionStatus();
};

#endif /*WINMIEXECNEXT_H_*/
