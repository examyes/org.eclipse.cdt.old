#ifndef MIEMPTYCOMMAND_H_
#define MIEMPTYCOMMAND_H_

#include "MICommand.h"

class MIEmptyCommand : public MICommand
{
public:
	MIEmptyCommand(MIEngine & engine, string & token)
		: MICommand(engine, token) { }
	virtual ~MIEmptyCommand() { }
	
	virtual void runCommand();

	virtual void sendResult(ostream & out);
};

#endif /*MIEMPTYCOMMAND_H_*/
