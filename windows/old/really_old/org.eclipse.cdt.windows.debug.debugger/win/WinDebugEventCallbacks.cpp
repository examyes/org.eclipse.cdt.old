#include "WinDebugEventCallbacks.h"

#include <iostream>
using namespace std;

STDMETHODIMP WinDebugEventCallbacks::GetInterestMask(PULONG mask) {
	*mask = DEBUG_EVENT_CREATE_PROCESS | DEBUG_EVENT_BREAKPOINT;
	return S_OK;
}

STDMETHODIMP WinDebugEventCallbacks::CreateProcess(
    __in ULONG64 ImageFileHandle,
    __in ULONG64 Handle,
    __in ULONG64 BaseOffset,
    __in ULONG ModuleSize,
    __in PCSTR ModuleName,
    __in PCSTR ImageName,
    __in ULONG CheckSum,
    __in ULONG TimeDateStamp,
    __in ULONG64 InitialThreadHandle,
    __in ULONG64 ThreadDataOffset,
    __in ULONG64 StartOffset)
{
	cerr << "Process created\n";
	return DEBUG_STATUS_BREAK;
}

STDMETHODIMP WinDebugEventCallbacks::Breakpoint(PDEBUG_BREAKPOINT Bp) {
	cerr << "Breakpoint hit\n";
	return DEBUG_STATUS_BREAK;
}
