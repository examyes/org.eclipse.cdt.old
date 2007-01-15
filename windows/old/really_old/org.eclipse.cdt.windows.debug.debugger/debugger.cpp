#include <windows.h>
#include <iostream>
using namespace std;
#include "win/WinDebugEngine.h"
#include "win/WinDebugHandler.h"
#include <MIHandler.h>

DWORD WINAPI miInputThread(void * arg);

int main(int argc, char **argv) {
	if (argc < 2) {
		wcerr << "usage: " << argv[0] << " target.exe" << endl;
		return 1;
	}

	WinDebugEngine engine(argv[1]);
	WinDebugHandler handler(engine);
	
	// Create thread for MI input loop
	if (!CreateThread(NULL, 0, miInputThread, &handler, 0, NULL)) {
		cerr << "Failed to create input thread\n";
		return 1;
	}
	
	engine.mainLoop();
	
	return 0;
}

DWORD WINAPI miInputThread(void * arg) {
	WinDebugHandler * handler = (WinDebugHandler *)arg;
	handler->inputLoop();
	return 0;
}
