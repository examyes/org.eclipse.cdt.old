#include <windows.h>
#include <iostream>
using namespace std;
#include "win/WinDebugEngine.h"
#include "winmi/WinMIEngine.h"

DWORD WINAPI miInputThread(void * arg);

int main(int argc, char **argv) {
	if (argc < 2) {
		cerr << "usage: " << argv[0] << " target.exe" << endl;
		return 1;
	}

//	for (int i = 1; i < argc; ++i) {
//		cerr << argv[i] << " ";
//	}
//	cerr << endl;
	
	WinDebugEngine debugEngine(argv[argc - 1]);
	WinMIEngine miEngine(debugEngine);
	
	// Create thread for MI input loop
	if (!CreateThread(NULL, 0, miInputThread, &miEngine, 0, NULL)) {
		cerr << "Failed to create input thread\n";
		return 1;
	}
	
	debugEngine.mainLoop();
	
	return 0;
}

DWORD WINAPI miInputThread(void * arg) {
	WinMIEngine * miEngine = (WinMIEngine *)arg;
	miEngine->inputLoop();
	return 0;
}
