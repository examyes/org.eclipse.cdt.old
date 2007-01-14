#ifndef WINDEBUGCOMMAND_H_
#define WINDEBUGCOMMAND_H_

class WinDebugCommand
{
public:
	virtual void execute(WinDebugHandler * handler) = 0;
};

#endif /*WINDEBUGCOMMAND_H_*/
