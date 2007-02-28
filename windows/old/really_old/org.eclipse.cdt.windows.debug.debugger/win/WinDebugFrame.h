#ifndef WINDEBUGFRAME_H_
#define WINDEBUGFRAME_H_

#include <string>
using namespace std;

struct WinDebugFrame {
	ULONG64 addr;
	string func;
	string file;
	ULONG line;
};

#endif /*WINDEBUGFRAME_H_*/
