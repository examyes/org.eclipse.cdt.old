#include "WinMIExecNext.h"
#include <WinDebugEngine.h>

WinMIExecNext::WinMIExecNext(MIEngine & miEngine, string & token)
: WinMIExecRun(miEngine, token) {
}

WinMIExecNext::~WinMIExecNext() {
}

ULONG WinMIExecNext::getExecutionStatus() {
	return 0; //DEBUG_STATUS_STEP_OVER;
}

void WinMIExecNext::execute(WinDebugEngine & debugEngine) {
//	if (FAILED(debugEngine.getDebugControl()->SetCodeLevel(DEBUG_LEVEL_SOURCE))) {
//		status = _error;
//		msg = "Failed to set code level";
//		return;
//	}
	
	WinMIExecRun::execute(debugEngine);
}
