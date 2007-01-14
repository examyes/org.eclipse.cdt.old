#include <windows.h>
#include <iostream>
using namespace std;
#include "win/WinDebugEngine.h"
#include "win/WinDebugHandler.h"
#include <MIHandler.h>


int wmain(int argc, wchar_t **argv) {
	if (argc < 2) {
		wcerr << L"usage: " << argv[0] << L" target.exe" << endl;
		return 1;
	}

	WinDebugEngine engine;
	WinDebugHandler handler(engine);
	handler.inputLoop();
	
	return 0;
}
