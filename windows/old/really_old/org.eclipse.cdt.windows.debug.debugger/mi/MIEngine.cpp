#include "MIEngine.h"

#include "MICommand.h"
#include "MIEmptyCommand.h"

#include <iostream>
#include <string>
using namespace std;

#include <string.h>
#include <windows.h>

class EndOfFile { };

void MIEngine::inputLoop() {
	try {
		while (true) {
			out << "(gdb) " << endl;
			MICommand * cmd = parseCommand();
			if (cmd)
				cmd->runCommand();
		}
	} catch (EndOfFile &) {
		MessageBox(NULL, "EOF", "EOF", NULL);
	}
}

MICommand * MIEngine::parseCommand() {
	// Get the token
	string token;
	parseToken(token);
	
	char c;
	if (!in.get(c))
		throw EndOfFile();

	// If CLI command, pass the whole line
	if (c != '-') {
		string operation;
		while (c != '\n') {
			operation += c;
			if (!in.get(c))
				throw EndOfFile();
		}
		return createCLICommand(token, operation);
	}

	// Now that that's out of the way, we have an MI command to parse
	
	// Get the operation name
	string operation;
	parseParameter(operation);
	
	// Create the command object
	MICommand * command = createMICommand(token, operation);

	// pass in the options/parameters
	while (true) {
		string parameter;
		parseParameter(parameter);
		if (parameter.empty())
			break;
		else
			command->addParameter(parameter);
	}
	
	// Done
	return command;
}

void MIEngine::parseToken(string & token) {
	char c;
	if (!in.get(c))
		throw EndOfFile();
	
	// Skip over white space
	while (isspace(c)) {
		if (!in.get(c))
			throw EndOfFile();
	}
	
	while (c >= '0' && c <= '9') {
		token += c;
		if (!in.get(c))
			throw EndOfFile();
	}

	in.unget();
}

void MIEngine::parseParameter(string & parameter) {
	char c;
	if (!in.get(c))
		throw EndOfFile();

	if (c == '"') {
		// scan to the next "
	} else {
		while (!isspace(c)) {
			parameter += c;
			if (!in.get(c))
				throw EndOfFile();
		}
	}
	
	// Skip over the next whitespace until EOL
	while (c != '\n' && isspace(c)) {
		if (!in.get(c))
			throw EndOfFile();
	}
	
	in.unget();
}

void MIEngine::skipLine() {
	char c;
	while (in.get(c)) {
		if (c == '\n')
			return;
	}
}

MICommand * MIEngine::createMICommand(string & token, string & operation) {
	return new MIEmptyCommand(*this, token);
}

MICommand * MIEngine::createCLICommand(string & token, string & operation) {
	return new MIEmptyCommand(*this, token);
}

void MIEngine::enqueueResult(MICommand * command) {
	// Todo wrap this with a mutex
	command->sendResult(out);
}
