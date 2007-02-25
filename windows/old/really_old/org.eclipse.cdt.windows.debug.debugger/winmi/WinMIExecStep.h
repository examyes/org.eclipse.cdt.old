#ifndef WINMIEXECSTEP_H_
#define WINMIEXECSTEP_H_

#include "WinMIExecRun.h"

class WinMIExecStep : public WinMIExecRun
{
public:
	WinMIExecStep(MIEngine & miEngine, string & token);
	virtual ~WinMIExecStep();
	
	virtual void execute(WinDebugEngine & debugEngine);

protected:
	virtual ULONG getExecutionStatus();
};

#endif /*WINMIEXECSTEP_H_*/
