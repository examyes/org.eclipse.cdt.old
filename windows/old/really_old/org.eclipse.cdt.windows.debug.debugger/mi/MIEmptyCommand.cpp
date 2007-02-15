#include "MIEmptyCommand.h"
#include "MIEngine.h"

void MIEmptyCommand::runCommand() {
	engine.enqueueResult(this);
}

void MIEmptyCommand::sendResult(ostream & out) {
	out << token << "^done" << endl;
}
