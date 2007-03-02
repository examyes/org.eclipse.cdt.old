#include "WinDebugEngine.h"
#include "WinDebugEventCallbacks.h"
#include "WinDebugCommand.h"
#include "WinDebugRunCommand.h"
#include "WinDebugFrame.h"

#include <iostream>
using namespace std;

void WinDebugEngine::message(const char * msg) {
	MessageBox(NULL, msg, "WinDebug", MB_OK);
}

WinDebugEngine::WinDebugEngine(char * _command)
: command(_command), currentRunCommand(NULL), frames(NULL), process(NULL) {
	commandMutex = CreateMutex(NULL, FALSE, NULL);
	commandReadyEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	
	// Init the dbgeng/dbghelp
	
	// Load in the DLL
	// TODO Need to find a way to not hard code the location
	HMODULE dbghelp = LoadLibrary("C:\\Program Files\\Debugging Tools for Windows\\dbghelp.dll");
	HMODULE dbgeng = LoadLibrary("C:\\Program Files\\Debugging Tools for Windows\\dbgeng.dll");
	
	typedef HANDLE (__stdcall * DebugCreateProc)(__in REFIID InterfaceId, __out PVOID* Interface);
	DebugCreateProc debugCreate = (DebugCreateProc)GetProcAddress(dbgeng, "DebugCreate");
	
	if (FAILED(debugCreate(__uuidof(IDebugClient), (void **)&debugClient))) {
		cerr << "Failed to create IDebugClient\n";
		return;
	}

	if (FAILED(debugClient->QueryInterface(__uuidof(IDebugControl), ((void **)&debugControl)))) {
		cerr << "Failed to create IDebugControl\n";
		return;
	}
	
	symSetOptions = (SymSetOptionsProc)GetProcAddress(dbghelp, "SymSetOptions");
	symInitialize = (SymInitializeProc)GetProcAddress(dbghelp, "SymInitialize");
	symFromAddr = (SymFromAddrProc)GetProcAddress(dbghelp, "SymFromAddr");
	symGetLineFromAddr64 = (SymGetLineFromAddr64Proc)GetProcAddress(dbghelp, "SymGetLineFromAddr64");
	
	typedef LPAPI_VERSION (__stdcall * ImagehlpApiVersionProc)();
	ImagehlpApiVersionProc getVersion = (ImagehlpApiVersionProc)GetProcAddress(dbghelp, "ImagehlpApiVersion");
	
	API_VERSION * version = getVersion();
	char buff[1024];
	sprintf_s(buff, "Version %d.%d.%d", version->MajorVersion, version->MinorVersion, version->Revision);
	message(buff);
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
		if (!symFromAddr(process, frames[fi].addr, &disp64, symbol)) {
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
		if (!symGetLineFromAddr64(process, frames[fi].addr, &disp, &lineInfo)) {
			if (!isValid) return false;
			--fi; --numFrames; continue;
		}
			
		frames[fi].file = lineInfo.FileName;
		frames[fi].line = lineInfo.LineNumber;
		isValid = true;
	}
	
	return isValid;
}
