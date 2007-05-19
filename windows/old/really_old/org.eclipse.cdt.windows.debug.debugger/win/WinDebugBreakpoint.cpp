#include "WinDebugBreakpoint.h"

WinDebugBreakpoint::~WinDebugBreakpoint()
{
}

bool WinDebugBreakpoint::setBreakpoint() {
	// first remember the contents we are replacing
	SIZE_T num;
	if (!ReadProcessMemory(process, (void *)address, &opcode, sizeof(opcode), &num))
		return false;
		
	if (num != sizeof(opcode))
		return false;
	
	// write the breakpoint interrupt instruction
	BYTE int3 = 0xcc;
	if (!WriteProcessMemory(process, (void *)address, &int3, sizeof(int3), &num))
		return false;
	
	if (num != sizeof(int3))
		return false;
	
	return true;
}

bool WinDebugBreakpoint::breakpointHit() {
	// put the contents back
	SIZE_T num;
	if (!WriteProcessMemory(process, (void *)address, &opcode, sizeof(opcode), &num))
		return false;
	
	if (num != sizeof(opcode))
		return false;
	
	if (!isTemp) {
		// set the trap
		// get the thread context
		// set the trap bit in eflags
		// set the thread context
	}
	
	return true;
}
