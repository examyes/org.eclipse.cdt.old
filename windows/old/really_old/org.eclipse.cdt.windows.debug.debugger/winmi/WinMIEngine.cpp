#include "WinMIEngine.h"
#include "../win/WinDebugEngine.h"

#include "WinMIExecRun.h"
#include "WinMIBreakInsert.h"
#include "WinMIStackInfoDepth.h"
#include "WinMIStackListFrames.h"

MICommand * WinMIEngine::createMICommand(string & token, string & operation) {
	if (operation == "exec-run" || operation == "exec-continue") {
		WinMIExecRun * cmd = new WinMIExecRun(*this, token);
		debugEngine.enqueueCommand(cmd);
		return cmd;
	} else if (operation == "break-insert") {
		WinMIBreakInsert * cmd = new WinMIBreakInsert(*this, token);
		debugEngine.enqueueCommand(cmd);
		return cmd;
	} else if (operation == "stack-info-depth") {
		WinMIStackInfoDepth * cmd = new WinMIStackInfoDepth(*this, token);
		debugEngine.enqueueCommand(cmd);
		return cmd;
	} else if (operation == "stack-list-frames") {
		WinMIStackListFrames * cmd = new WinMIStackListFrames(*this, token);
		debugEngine.enqueueCommand(cmd);
		return cmd;
	} else
		return MIEngine::createMICommand(token, operation);
}
