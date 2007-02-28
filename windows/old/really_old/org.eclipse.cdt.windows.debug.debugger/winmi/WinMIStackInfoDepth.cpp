#include "WinMIStackInfoDepth.h"
#include <WinDebugEngine.h>
#include <MIEngine.h>

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
	depth = debugEngine.getNumFrames();
	engine.enqueueResult(this);
}
