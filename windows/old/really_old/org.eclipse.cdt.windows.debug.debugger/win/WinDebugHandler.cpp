#include "WinDebugHandler.h"
#include "WinDebugExecAbort.h"

MICommand * WinDebugHandler::createCommand(string & operation) {
	return MIHandler::createCommand(operation);
}

MIExecAbort * WinDebugHandler::createExecAbort() {
	return new WinDebugExecAbort();
}
