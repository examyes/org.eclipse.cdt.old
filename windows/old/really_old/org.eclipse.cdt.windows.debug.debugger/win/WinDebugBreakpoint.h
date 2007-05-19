#ifndef WINDEBUGBREAKPOINT_H_
#define WINDEBUGBREAKPOINT_H_

#include <windows.h>
#include <string>

using namespace std;

class WinDebugEngine;

class WinDebugBreakpoint
{
public:
	WinDebugBreakpoint(HANDLE _process, DWORD64 _address, string & _function,
			string & _filename, DWORD _line, bool _isTemp)
		: process(_process), address(_address), function(_function),
		  filename(_filename), line(_line), isTemp(_isTemp) { }
	virtual ~WinDebugBreakpoint();
	
	DWORD64 getAddress() { return address; }
	string & getFunction() { return function; }
	string & getFilename() { return filename; }
	DWORD getLine() { return line; }
	
	int getId() { return id; }
	void setId(int _id) { id = _id; }
	
	// set the breakpoint in memory
	bool setBreakpoint();
	
	// put the opcode back and set the trap (if not temporary)
	bool breakpointHit();
	
private:
	int id;
	HANDLE process;
	DWORD64 address;
	string function;
	string filename;
	DWORD line;
	bool isTemp;
	
	BYTE opcode;
};

#endif /*WINDEBUGBREAKPOINT_H_*/
