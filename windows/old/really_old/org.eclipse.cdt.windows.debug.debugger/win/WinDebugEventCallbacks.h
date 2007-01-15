#ifndef WINDEBUGEVENTCALLBACKS_H_
#define WINDEBUGEVENTCALLBACKS_H_

#include <dbgeng.h>

class WinDebugEventCallbacks : public DebugBaseEventCallbacks
{
public:
	WinDebugEventCallbacks() { }
	virtual ~WinDebugEventCallbacks() { }
	
    STDMETHOD_(ULONG, AddRef)() { return 0; }
    STDMETHOD_(ULONG, Release)() { return 0; }
    STDMETHOD(GetInterestMask)(PULONG mask);
    
    STDMETHOD(CreateProcess)(
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
            __in ULONG64 StartOffset
            );

    STDMETHOD(Breakpoint)(PDEBUG_BREAKPOINT Bp);
};

#endif /*WINDEBUGEVENTCALLBACKS_H_*/
