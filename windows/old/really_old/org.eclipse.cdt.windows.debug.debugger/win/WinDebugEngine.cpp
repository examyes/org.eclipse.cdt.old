#include "WinDebugEngine.h"
#include "WinDebugEventCallbacks.h"
#include "WinDebugCommand.h"

#include <iostream>
using namespace std;

WinDebugEngine::WinDebugEngine(char * _command)
: command(_command), currentRunCommand(NULL) {
	commandMutex = CreateMutex(NULL, FALSE, NULL);
	commandReadyEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
}

void WinDebugEngine::enqueueCommand(WinDebugCommand * command) {
	WaitForSingleObject(commandMutex, INFINITE);
	commandQueue.push_back(command);
	SetEvent(commandReadyEvent);
	ReleaseMutex(commandMutex);
}

void WinDebugEngine::run(WinDebugRunCommand * runCommand) {
	currentRunCommand = runCommand;
}

void WinDebugEngine::mainLoop() {
	// Get the objects
	if (FAILED(DebugCreate(__uuidof(IDebugClient), (void **)&debugClient))) {
		cerr << "Failed to create IDebugClient\n";
		return;
	}

	if (FAILED(debugClient->QueryInterface(__uuidof(IDebugControl), ((void **)&debugControl)))) {
		cerr << "Failed to create IDebugControl\n";
		return;
	}
	
	WinDebugEventCallbacks eventCallbacks;
	if (FAILED(debugClient->SetEventCallbacks(&eventCallbacks))) {
		cerr << "Failed to set callbacks\n";
		return;
	}
	
	// Create the process
	if (FAILED(debugClient->CreateProcess(0, command, DEBUG_PROCESS | CREATE_NO_WINDOW))) {
		cerr << "Failed to create process for: " << command << endl;
		return;
	}
	
	while (true) {
		debugControl->SetExecutionStatus(DEBUG_STATUS_GO);
		HRESULT hr = debugControl->WaitForEvent(0, INFINITE);
		
		if (hr != S_OK) {
			return;
		}
		
		while (true) {
			// Wait for commands
			WaitForSingleObject(commandReadyEvent, INFINITE);
			// Grab the mutex for the queue
			WaitForSingleObject(commandMutex, INFINITE);
			
			if (commandQueue.empty()) {
				// No commands
				ResetEvent(commandReadyEvent);
				ReleaseMutex(commandMutex);
				if (currentRunCommand)
					// Ready to run
					break;
				else
					// Wait for more commands
					continue;
			}

			// Pop the next command and run it
			WinDebugCommand * cmd = commandQueue.front();
			commandQueue.pop_front();
			ReleaseMutex(commandMutex);
			
			cmd->execute(*this);
		}
	}
}