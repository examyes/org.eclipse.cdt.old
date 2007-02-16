#include "WinMIStackInfoDepth.h"
#include <WinDebugEngine.h>
#include <MIEngine.h>
#include <dbgeng.h>

WinMIStackInfoDepth::WinMIStackInfoDepth(MIEngine & miEngine, string & token)
: MICommand(miEngine, token), error(false), depth(0) {
}

WinMIStackInfoDepth::~WinMIStackInfoDepth() {
}

void WinMIStackInfoDepth::runCommand() {
}

void WinMIStackInfoDepth::sendResult(ostream & out) {
	out << token;
	if (error) {
		out << "^error,msg=\"" << msg << '"';
	} else {
		out << "^done,depth=\"" << depth << '"';
	}
	out << endl;
}

void WinMIStackInfoDepth::execute(WinDebugEngine & debugEngine) {
	DEBUG_STACK_FRAME frames[50];
	if (FAILED(debugEngine.getDebugControl()
			->GetStackTrace(0, 0, 0, frames, 50, &depth))) {
		error = true;
		msg = "Failed to get stack trace";
		return;
	}
	engine.enqueueResult(this);
}
