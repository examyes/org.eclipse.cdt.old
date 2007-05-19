#include "WinMIBreakInsert.h"
#include <MIEngine.h>
#include <WinDebugEngine.h>

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
//			<< ",enabled=\"" << (flags & DEBUG_BREAKPOINT_ENABLED ? "y" : "n") << '"'
			<< ",addr=\"" << offset << '"'
			<< ",file=\"" << file << '"'
			<< ",line=\"" << line << '"'
			<< ",times=\"0\""
		<< '}'
		<< endl;
}

void WinMIBreakInsert::execute(WinDebugEngine & debugEngine) {
	
	char buffer[sizeof(SYMBOL_INFO) + MAX_SYM_NAME];
	SYMBOL_INFO * symbol = (SYMBOL_INFO *)buffer;

	symbol->SizeOfStruct = sizeof(SYMBOL_INFO);
	symbol->MaxNameLen = MAX_SYM_NAME;

	if (!SymFromName(debugEngine.getProcess(), target.c_str(), symbol)) {
		char buff[256];
		sprintf_s(buff, "Failed to get symbol info: %d", GetLastError());
		recordError(buff);
		return;
	}
	
	offset = symbol->Address;
	
	IMAGEHLP_LINE64 lineInfo;
	lineInfo.SizeOfStruct = sizeof(lineInfo);
	DWORD disp;
	if (!SymGetLineFromAddr64(debugEngine.getProcess(), offset, &disp, &lineInfo)) {
		char buff[256];
		sprintf_s(buff, "Failed to get line info: %d", GetLastError());
		recordError(buff);
		return;
	}
	
	file = lineInfo.FileName;
	line = lineInfo.LineNumber;
	
	engine.enqueueResult(this);
}

void WinMIBreakInsert::recordError(const char * _msg) {
	error = true;
	msg = _msg;
	engine.enqueueResult(this);
}
