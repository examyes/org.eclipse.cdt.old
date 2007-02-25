#include "WinMIEngine.h"
#include "../win/WinDebugEngine.h"

#include "WinMIExecRun.h"
#include "WinMIExecNext.h"
#include "WinMIExecStep.h"
#include "WinMIBreakInsert.h"
#include "WinMIStackInfoDepth.h"
#include "WinMIStackListFrames.h"

#define COMMAND(X) X * cmd = new X(*this, token); debugEngine.enqueueCommand(cmd); return cmd

MICommand * WinMIEngine::createMICommand(string & token, string & operation) {
	if (operation == "exec-run" || operation == "exec-continue") {
		COMMAND(WinMIExecRun);
	} else if (operation == "exec-next") {
		COMMAND(WinMIExecNext);
	} else if (operation == "exec-step") {
		COMMAND(WinMIExecStep);
	} else if (operation == "break-insert") {
		COMMAND(WinMIBreakInsert);
	} else if (operation == "stack-info-depth") {
		COMMAND(WinMIStackInfoDepth);
	} else if (operation == "stack-list-frames") {
		COMMAND(WinMIStackListFrames);
	} else
		return MIEngine::createMICommand(token, operation);
}
