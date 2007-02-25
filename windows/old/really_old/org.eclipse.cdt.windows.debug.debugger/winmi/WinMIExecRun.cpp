#include "WinMIExecRun.h"
#include <MIEngine.h>
#include <WinDebugEngine.h>
#include <dbgeng.h>

ULONG WinMIExecRun::getExecutionStatus() {
	return DEBUG_STATUS_GO;
}

void WinMIExecRun::execute(WinDebugEngine & debugEngine) {
	if (FAILED(debugEngine.getDebugControl()->SetExecutionStatus(getExecutionStatus()))) {
		status = _error;
		msg = "Failed to set execution status";
		return;
	}

	debugEngine.run(this);
	status = _running;
	engine.enqueueResult(this);
}

void WinMIExecRun::stopped(WinDebugEngine & debugEngine) {
	status = _stopped;
	engine.enqueueResult(this);
}

void WinMIExecRun::runCommand() {
}

void WinMIExecRun::sendResult(ostream & out) {
	out << token;
	switch (status) {
	case _running:
		out << "^running";
		break;
	case _stopped:
		out << "*stopped";
		break;
	case _error:
		out << "^error,msg=\"" << msg << '"';
		break;
	default:
		out << "^error,msg=\"Bad run state\"";
	}
	out << endl;
}
