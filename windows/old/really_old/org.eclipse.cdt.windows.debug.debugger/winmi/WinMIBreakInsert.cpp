#include "WinMIBreakInsert.h"
#include <MIEngine.h>
#include <WinDebugEngine.h>
#include <dbgeng.h>

WinMIBreakInsert::WinMIBreakInsert(MIEngine & miEngine, string & token)
: MICommand(miEngine, token)
, error(false)
, isTemporary(false) { 

}

void WinMIBreakInsert::addParameter(string & parameter) {
	if (parameter[0] == '-') {
		if (parameter == "-t")
			isTemporary = true;
	} else {
		target = parameter;
	}
}

void WinMIBreakInsert::runCommand() {
}

void WinMIBreakInsert::sendResult(ostream & out) {
	if (error) {
		out << token << "^error,msg=\"" << msg << '"' << endl;
		return;
	}
	
	out << token << "^done"
		<< ",bkpt={"
			<< "number=\"" << id << '"'
			<< ",type=\"breakpoint\""
			<< ",func=\"" << target << '"'
			<< ",disp=\"" << (isTemporary ? "del" : "keep") << '"'
			<< ",enabled=\"" << (flags & DEBUG_BREAKPOINT_ENABLED ? "y" : "n") << '"'
			<< ",addr=\"" << offset << '"'
			<< ",file=\"" << file << '"'
			<< ",line=\"" << line << '"'
			<< ",times=\"0\""
		<< '}'
		<< endl;
}

void WinMIBreakInsert::execute(WinDebugEngine & debugEngine) {
	IDebugBreakpoint * breakpoint;
	if (FAILED(debugEngine.getDebugControl()
			->AddBreakpoint(DEBUG_BREAKPOINT_CODE, DEBUG_ANY_ID, &breakpoint))) {
		recordError("Failed to add breakpoint");
		return;
	}
	
	if (FAILED(breakpoint->SetOffsetExpression(target.c_str()))) {
		recordError("Failed to set breakpoint expression");
		return;
	}
	
	flags = DEBUG_BREAKPOINT_ENABLED;
	if (isTemporary)
		flags |= DEBUG_BREAKPOINT_ONE_SHOT;
	if (FAILED(breakpoint->AddFlags(flags))) {
		recordError("Failed to set breakpoint flags");
		return;
	}
	
	if (FAILED(breakpoint->GetFlags(&flags))) {
		recordError("Failed to get breakpoint flags");
		return;
	}
	
	if (FAILED(breakpoint->GetId(&id))) {
		recordError("Failed to get breakpoint id");
		return;
	}
	
	if (FAILED(breakpoint->GetOffset(&offset))) {
		recordError("Failed to get breakpoint offset");
		return;
	}

	char buff[MAX_PATH];
	if (FAILED(debugEngine.getDebugSymbols()
			->GetLineByOffset(offset, &line, buff, MAX_PATH, NULL, NULL))) {
		recordError("Failed to get breakpoint file/line");
		return;
	}
	file = buff;

	engine.enqueueResult(this);
}

void WinMIBreakInsert::recordError(const char * _msg) {
	error = true;
	msg = _msg;
	engine.enqueueResult(this);
}
