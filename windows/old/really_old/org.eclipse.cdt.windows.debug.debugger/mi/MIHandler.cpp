#include "MIHandler.h"

#include "MICommand.h"
#include "MIExecAbort.h"

#include <iostream>
#include <string>
using namespace std;

void MIHandler::inputLoop() {
	while (parseCommand());
}

bool MIHandler::parseCommand() {
	// Get the token
	int token = parseToken();
	if (token < -1) { // EOF
		return false;
	}
	
	char c;
	if (!in.get(c))
		return false;

	// Make sure it's a MI command
	if (c != '-') {
		// Evil CLI command, probably, not supported since this isn't GDB
		skipLine();
		reportUnsupportedCommand();
		return true;
	}

	// Get the operation name
	string operation;
	if (!parseOperation(operation)) {
		return false;
	}
	
	// Create the command object
	MICommand * command = createCommand(operation);
	if (token >= 0)
		command->setToken(token);
	
	// pass in the options/parameters
	while (in.get(c)) {
		if (c == '\n')
			return true;
		else if (c == '\r')
			//skip
			continue;
		
	}

	// EOF missing nl, return null
	return false;
}

int MIHandler::parseToken() {
	char c;
	if (!in.get(c))
		return -2;
	
	if (c >= '0' && c <= '9') {
		// We have a token
		int token = c - '0';
		while (in.get(c)) {
			if (c >= '0' && c <= '9') {
				token = token * 10 + c - '0';
			} else {
				in.unget();
				return token;
			}
		}
		return -2; // EOF before operation, bad news.
	} else {
		// No we don't
		in.unget();
		return -1;
	}
}

bool MIHandler::parseOperation(string & operation) {
	char c;
	while (in.get(c)) {
		if (c == ' ' || c == '\r' || c == '\n') {
			in.unget();
			return true;
		} else
			operation += c;
	}
	// EOF before end of line
	return false;
}

void MIHandler::skipLine() {
	char c;
	while (in.get(c)) {
		if (c == '\n')
			return;
	}
}

void MIHandler::reportUnsupportedCommand() {
	
}

MICommand * MIHandler::createCommand(string & operation) {
	if (operation == "exec-abort")
		return createExecAbort();
	else
		return NULL;
}
