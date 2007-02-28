#include "WinDebugEngine.h"
#include "WinDebugEventCallbacks.h"
#include "WinDebugCommand.h"
#include "WinDebugRunCommand.h"
#include "WinDebugFrame.h"

#include <iostream>
using namespace std;

#include <dbghelp.h>

WinDebugEngine::WinDebugEngine(char * _command)
: command(_command), currentRunCommand(NULL), frames(NULL) {
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

typedef HANDLE (*DebugCreateProc)(__in REFIID InterfaceId, __out PVOID* Interface);

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
	
	WinDebugEventCallbacks eventCallbacks(*this);
	if (FAILED(debugClient->SetEventCallbacks(&eventCallbacks))) {
		cerr << "Failed to set callbacks" << endl;
		return;
	}
	
	// Create the process
	if (FAILED(debugClient->CreateProcess(0, command, DEBUG_ONLY_THIS_PROCESS | CREATE_NO_WINDOW))) {
		cerr << "Failed to create process for: " << command << endl;
		return;
	}

	while (true) {
		HRESULT hr = debugControl->WaitForEvent(0, INFINITE);
		if (hr != S_OK) {
			return;
		}
		
		if (currentRunCommand) {
			if (!populateFrames()) {
				// This is not a valid frame, keep going
				debugControl->SetExecutionStatus(DEBUG_STATUS_STEP_OVER);
				continue;
			}			
			
			currentRunCommand->stopped(*this);
			currentRunCommand = NULL;
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

void WinDebugEngine::processCreated(HANDLE _process) {
	// Stow away the process handle 
	process = _process;
	
	// Set up dbghelp
	SymSetOptions(SYMOPT_LOAD_LINES | SYMOPT_UNDNAME | SYMOPT_DEFERRED_LOADS);
	
	if (!SymInitialize(process, NULL, TRUE)) {
		cerr << "Failed to init symbol handler" << endl;
		return;
	}

}

bool WinDebugEngine::populateFrames() {
	// The first stack frame must be valid
	bool isValid = false;
	
	DEBUG_STACK_FRAME stack[50];
	if (FAILED(debugControl->GetStackTrace(0, 0, 0, stack, 50, &numFrames))) {
		MessageBox(NULL, "GetStackTrace failed", "WinDebug", MB_OK);
		return false;
	}

	// Take the top three off since they are in the runtime
	// TODO - usually that is. we should be smarter about this.
	numFrames -= 3;
	
	if (numFrames < 1) {
		return false;
	}
	
	if (frames)
		delete[] frames;
	frames = new WinDebugFrame[numFrames];
		
	int fi = 0;
	for (int i = 0; i < numFrames; ++fi, ++i) {
		frames[fi].addr = stack[i].InstructionOffset;
			
		// Get the name
		char buffer[sizeof(SYMBOL_INFO) + MAX_SYM_NAME];
		SYMBOL_INFO * symbol = (SYMBOL_INFO *)buffer;
			
		symbol->SizeOfStruct = sizeof(SYMBOL_INFO);
		symbol->MaxNameLen = MAX_SYM_NAME;

		DWORD64 disp64;
		if (!SymFromAddr(process, frames[fi].addr, &disp64, symbol)) {
			MessageBox(NULL, "SymFromAddr failed", "WinDebug", MB_OK);
			if (!isValid) return false;
			--fi; --numFrames; continue;
		}
			
		if (symbol->Flags & SYMFLAG_EXPORT) {
			if (!isValid) return false;
			--fi; --numFrames; continue;
		}

		frames[fi].func = symbol->Name;
			
		// Get the file/line
		DWORD disp;
		IMAGEHLP_LINE64 lineInfo;
		if (!SymGetLineFromAddr64(process, frames[fi].addr, &disp, &lineInfo)) {
			if (!isValid) return false;
			--fi; --numFrames; continue;
		}
			
		frames[fi].file = lineInfo.FileName;
		frames[fi].line = lineInfo.LineNumber;
		isValid = true;
	}
	
	return isValid;
}
