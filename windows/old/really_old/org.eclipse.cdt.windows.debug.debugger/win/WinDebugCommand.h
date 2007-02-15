#ifndef WINDEBUGCOMMAND_H_
#define WINDEBUGCOMMAND_H_

class WinDebugEngine;

class WinDebugCommand
{
public:
	virtual void execute(WinDebugEngine & engine) = 0;
};

#endif /*WINDEBUGCOMMAND_H_*/
