#include "WinMIExecStep.h"
#include <dbgeng.h>
#include <WinDebugEngine.h>

WinMIExecStep::WinMIExecStep(MIEngine & miEngine, string & token)
: WinMIExecRun(miEngine, token) {
}

WinMIExecStep::~WinMIExecStep() {
}

ULONG WinMIExecStep::getExecutionStatus() {
	return DEBUG_STATUS_STEP_INTO;
}

void WinMIExecStep::execute(WinDebugEngine & debugEngine) {
	if (FAILED(debugEngine.getDebugControl()->SetCodeLevel(DEBUG_LEVEL_SOURCE))) {
		status = _error;
		msg = "Failed to set code level";
		return;
	}
	
	WinMIExecRun::execute(debugEngine);
}
