#ifndef WINDEBUGRUNCOMMAND_H_
#define WINDEBUGRUNCOMMAND_H_

#include "WinDebugCommand.h"

class WinDebugRunCommand : public WinDebugCommand {
public:
	virtual void stopped() = 0;
};

#endif /*WINDEBUGRUNCOMMAND_H_*/
