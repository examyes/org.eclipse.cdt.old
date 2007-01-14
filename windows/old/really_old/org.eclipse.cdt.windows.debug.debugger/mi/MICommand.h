#ifndef MICOMMAND_H_
#define MICOMMAND_H_

#include <iostream>
using namespace std;

class MIHandler;

class MICommand
{
public:
	void setToken(int _token) { tokenSet = true; token = _token; }
	
	// Send the result to the MI output stream
	virtual void sendResult(ostream & out);

private:
	bool tokenSet;
	int token;
};

#endif /*MICOMMAND_H_*/
