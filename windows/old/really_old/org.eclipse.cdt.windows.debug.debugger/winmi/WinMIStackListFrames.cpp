#include "WinMIStackListFrames.h"
#include <WinDebugEngine.h>
#include <MIEngine.h>

WinMIStackListFrames::WinMIStackListFrames(MIEngine & miEngine, string & token)
:MICommand(miEngine, token), paramState(param0), error(false), depth(0) {
}

WinMIStackListFrames::~WinMIStackListFrames() {
}

void WinMIStackListFrames::addParameter(string & parameter) {
	switch (paramState) {
	case param0:
		// skip the first once since this is usually 0
		// TODO fix later
		paramState = param1;
		break;
	case param1:
		sscanf_s(parameter.c_str(), "%d", &depth);
		paramState = param2;
		break;
	case param2:
		// should be an error
		break;
	}
}

void WinMIStackListFrames::runCommand() {
}

void WinMIStackListFrames::sendResult(ostream & out) {
	out << token;
	if (error) {
		out << "^error,msg=\"" << msg << '"';
	} else {
		out << "^done,stack=[";
		for (int i = 0; i < depth; ++i) {
			out << "frame={"
				<< "level=\"" << i << '"'
				<< ",addr=\"" << frames[i].addr << '"'
				<< ",func=\"" << frames[i].func << '"'
				<< ",file=\"" << frames[i].file << '"'
				<< ",line=\"" << frames[i].line << '"'
				<< "}";
			if (i + 1 < depth)
				out << ',';
		}
		out << "]";
	}
	out << endl;
}

void WinMIStackListFrames::execute(WinDebugEngine & debugEngine) {
	if (depth > 0) {
		DEBUG_STACK_FRAME * stack = new DEBUG_STACK_FRAME[depth];
		if (FAILED(debugEngine.getDebugControl()
				->GetStackTrace(0, 0, 0, stack, depth, &depth))) {
			error = true;
			msg = "Failed to get stack trace";
			return;
		}
		// Take three off 
		depth -= 3;

		// Variables we'll use
		IDebugSymbols * debugSymbols = debugEngine.getDebugSymbols();
		char buff[1024];
		frames = new Frame[depth];
		
		for (int i = 0; i < depth; ++i) {
			frames[i].addr = stack[i].InstructionOffset;

			// Get the function name at the instruction offset
			char func[1024];
			if (FAILED(debugSymbols->GetNameByOffset(stack[i].InstructionOffset,
					buff, sizeof(buff), NULL, NULL))) {
				error = true;
				msg = "Failed to get function name";
				return;
			}
			frames[i].func = buff;

			if (FAILED(debugSymbols->GetLineByOffset(stack[i].InstructionOffset,
					&frames[i].line, buff, sizeof(buff), NULL, NULL))) {
				error = true;
				msg = "Failed to get file/line info";
				return;
			}
			frames[i].file = buff;
		}
	}
	engine.enqueueResult(this);
}

