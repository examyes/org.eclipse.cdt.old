#include "WinDebugEngine.h"
#include "WinDebugEventCallbacks.h"

#include <iostream>
using namespace std;

void WinDebugEngine::enqueueCommand(WinDebugCommand * command) {
	
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
	cerr << "Creating: " << command << endl;
	if (FAILED(debugClient->CreateProcess(0, command, DEBUG_PROCESS | CREATE_NO_WINDOW))) {
		cerr << "Failed to create process for: " << command << endl;
		return;
	}
	
	while (true) {
		cerr << "Waiting\n";
		debugControl->SetExecutionStatus(DEBUG_STATUS_GO);
		HRESULT hr = debugControl->WaitForEvent(0, INFINITE);
		cerr << "Done\n";
		
		if (hr == E_PENDING) {
			cerr << "E_PENDING\n";
			return;
		} else if (hr == E_UNEXPECTED) {
			cerr << "E_UNEXPECTED\n";
			return;
		} else if (hr != S_OK) {
			cerr << "Not S_OK\n";
			return;
		}
	}
}
