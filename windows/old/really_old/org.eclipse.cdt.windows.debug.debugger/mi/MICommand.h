#ifndef MICOMMAND_H_
#define MICOMMAND_H_

#include <string>
#include <ostream>
using namespace std;

class MIEngine;

class MICommand
{
public:
	MICommand(MIEngine & _engine, string & _token)
		: engine(_engine), token(_token) { }

	virtual void addParameter(string & parameter);
	
	virtual void runCommand() = 0;

	virtual void sendResult(ostream & out) = 0;

protected:
	string token;
	MIEngine & engine;
};

#endif /*MICOMMAND_H_*/
