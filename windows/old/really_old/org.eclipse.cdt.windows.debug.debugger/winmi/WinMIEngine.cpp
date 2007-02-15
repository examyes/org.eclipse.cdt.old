#include "WinMIEngine.h"
#include "WinMIExecRun.h"
#include "../win/WinDebugEngine.h"

MICommand * WinMIEngine::createMICommand(string & token, string & operation) {
	if (operation == "exec-run") {
		debugEngine.enqueueCommand(new WinMIExecRun(*this, token));
		return NULL; // The MI engine won't run this one
	} else
		return MIEngine::createMICommand(token, operation);
}
