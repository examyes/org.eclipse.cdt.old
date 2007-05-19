#include "WinDebugEngine.h"
#include "WinDebugCommand.h"
#include "WinDebugRunCommand.h"
#include "WinDebugFrame.h"

#include <dbghelp.h>

#include <iostream>
using namespace std;

void WinDebugEngine::message(const char * msg) {
	MessageBox(NULL, msg, "WinDebug", MB_OK);
}

WinDebugEngine::WinDebugEngine(char * _command) :
	command(_command), currentRunCommand(NULL), frames(NULL) {
	commandMutex = CreateMutex(NULL, FALSE, NULL);
	commandReadyEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
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
	STARTUPINFO si;
	ZeroMemory(&si, sizeof(si));
	si.cb = sizeof(si);

	PROCESS_INFORMATION pi;
	if (!CreateProcess(NULL, command, NULL, NULL, FALSE,
			DEBUG_ONLY_THIS_PROCESS | CREATE_NO_WINDOW,
			NULL, NULL, &si, &pi)) {
		cerr << "Failed to create process for: " << command << endl;
		return;
	}

	while (true) {
		DEBUG_EVENT event;
		if (!WaitForDebugEvent(&event, INFINITE)) {
			cerr << "Wait for debug event failed" << endl;
			return;
		}

		// Handle the event
		DWORD continueStatus = handleEvent(event);
		ContinueDebugEvent(event.dwProcessId, event.dwThreadId, continueStatus);

		// Process any awaiting commands

		while (true) {
			// Grab a command
			WinDebugCommand * cmd= NULL;
			WaitForSingleObject(commandMutex, INFINITE);
			if (!commandQueue.empty()) {
				cmd = commandQueue.front();
				commandQueue.pop_front();
			}
			ReleaseMutex(commandMutex);

			if (cmd != NULL) {
				cmd->execute(*this);
			} else if (currentRunCommand != NULL) {
				// never mind, we'll just continue
				break;
			} else {
				// Wait for one
				WaitForSingleObject(commandReadyEvent, INFINITE);
			}
		}
	}
}

DWORD WinDebugEngine::handleEvent(DEBUG_EVENT & event) {

	switch (event.dwDebugEventCode) {
	case EXCEPTION_DEBUG_EVENT:
		// Process the exception code. When handling 
		// exceptions, remember to set the continuation 
		// status parameter (dwContinueStatus). This value 
		// is used by the ContinueDebugEvent function. 

		switch (event.u.Exception.ExceptionRecord.ExceptionCode) {
		case EXCEPTION_ACCESS_VIOLATION:
			// First chance: Pass this on to the system. 
			// Last chance: Display an appropriate error. 
			break;

		case EXCEPTION_BREAKPOINT:
			// First chance: Display the current 
			// instruction and register values. 
			break;

		case EXCEPTION_DATATYPE_MISALIGNMENT:
			// First chance: Pass this on to the system. 
			// Last chance: Display an appropriate error. 
			break;

		case EXCEPTION_SINGLE_STEP:
			// First chance: Update the display of the 
			// current instruction and register values. 
			break;

		case DBG_CONTROL_C:
			// First chance: Pass this on to the system. 
			// Last chance: Display an appropriate error. 
			break;

		default:
			// Handle other exceptions. 
			break;
		}

	case CREATE_THREAD_DEBUG_EVENT:
		// As needed, examine or change the thread's registers 
		// with the GetThreadContext and SetThreadContext functions; 
		// and suspend and resume thread execution with the 
		// SuspendThread and ResumeThread functions. 

		// dwContinueStatus = OnCreateThreadDebugEvent(DebugEv);
		break;

	case CREATE_PROCESS_DEBUG_EVENT:
	{
		CREATE_PROCESS_DEBUG_INFO & info = event.u.CreateProcessInfo;
		process = info.hProcess;
		
		SymSetOptions(SYMOPT_DEFERRED_LOADS | SYMOPT_LOAD_LINES | SYMOPT_UNDNAME);

		if (!SymInitialize(process, NULL, FALSE)) {
			cerr << "Failed to SymInitialize: " << GetLastError()<< endl;
			break;
		}

		if (!SymLoadModuleEx(process, info.hFile, (char *)info.lpImageName, NULL, 
				NULL, NULL, NULL, 0)) {
			cerr << "Failed to load initial module: " << GetLastError() << endl;
			break;
		}
		break;
	}
	
	case EXIT_THREAD_DEBUG_EVENT:
		// Display the thread's exit code. 

		// dwContinueStatus = OnExitThreadDebugEvent(DebugEv);
		break;

	case EXIT_PROCESS_DEBUG_EVENT:
		// Display the process's exit code. 

		// dwContinueStatus = OnExitProcessDebugEvent(DebugEv);
		break;

	case LOAD_DLL_DEBUG_EVENT:
		// Read the debugging information included in the newly 
		// loaded DLL. Be sure to close the handle to the loaded DLL 
		// with CloseHandle.

		// dwContinueStatus = OnLoadDllDebugEvent(DebugEv);
		break;

	case UNLOAD_DLL_DEBUG_EVENT:
		// Display a message that the DLL has been unloaded. 

		// dwContinueStatus = OnUnloadDllDebugEvent(DebugEv);
		break;

	case OUTPUT_DEBUG_STRING_EVENT:
		// Display the output debugging string. 

		// dwContinueStatus = OnOutputDebugStringEvent(DebugEv);
		break;

	case RIP_EVENT:
		// dwContinueStatus = OnRipEvent(DebugEv);
		break;
	}

	return DBG_CONTINUE;
}

bool WinDebugEngine::populateFrames() {
	// The first stack frame must be valid
	bool isValid = false;

	//	DEBUG_STACK_FRAME stack[50];
	//	if (FAILED(debugControl->GetStackTrace(0, 0, 0, stack, 50, &numFrames))) {
	//		MessageBox(NULL, "GetStackTrace failed", "WinDebug", MB_OK);
	//		return false;
	//	}

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
	for (int i = 0; i < numFrames;++fi, ++i) {
		//		frames[fi].addr = stack[i].InstructionOffset;

		// Get the name
		char buffer[sizeof(SYMBOL_INFO) + MAX_SYM_NAME];
		SYMBOL_INFO * symbol = (SYMBOL_INFO *)buffer;

		symbol->SizeOfStruct = sizeof(SYMBOL_INFO);
		symbol->MaxNameLen = MAX_SYM_NAME;

		DWORD64 disp64;
		if (!SymFromAddr(process, frames[fi].addr, &disp64, symbol)) {
			MessageBox(NULL, "SymFromAddr failed", "WinDebug", MB_OK);
			if (!isValid)
				return false;
			--fi;
			--numFrames;
			continue;
		}

		if (symbol->Flags & SYMFLAG_EXPORT) {
			if (!isValid)
				return false;
			--fi;
			--numFrames;
			continue;
		}

		frames[fi].func = symbol->Name;

		// Get the file/line
		DWORD disp;
		IMAGEHLP_LINE64 lineInfo;
		if (!SymGetLineFromAddr64(process, frames[fi].addr, &disp, &lineInfo)) {
			if (!isValid)
				return false;
			--fi;
			--numFrames;
			continue;
		}

		frames[fi].file = lineInfo.FileName;
		frames[fi].line = lineInfo.LineNumber;
		isValid = true;
	}

	return isValid;
}

void WinDebugEngine::addBreakpoint(WinDebugBreakpoint * bp) {
	
}
