#include "WinMIStackListFrames.h"
#include <WinDebugEngine.h>
#include <WinDebugFrame.h>
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
	int dd = debugEngine.getNumFrames();
	if (dd < depth)
		depth = dd;

	WinDebugFrame * df = debugEngine.getFrames();
	frames = new WinDebugFrame[depth];
	for (int i = 0; i < depth; ++i)
		frames[i] = df[i];
	
	engine.enqueueResult(this);
}

