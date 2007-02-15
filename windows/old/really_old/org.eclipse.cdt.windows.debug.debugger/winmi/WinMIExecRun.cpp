#include "WinMIExecRun.h"
#include <MIEngine.h>
#include "../win/WinDebugEngine.h"

void WinMIExecRun::execute(WinDebugEngine & debugEngine) {
	debugEngine.run(this);
	status = _running;
	engine.enqueueResult(this);
}

void WinMIExecRun::stopped() {
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
	default:
		out << "^error";
	}
	out << endl;
}
