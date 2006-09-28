#ifndef DEBUGEVENTCALLBACKS_H_
#define DEBUGEVENTCALLBACKS_H_

class DebugEventCallbacks : public DebugBaseEventCallbacksWide
{
private:
	jobject ref;

public:
	DebugEventCallbacks(JNIEnv * env, jobject obj);
	virtual ~DebugEventCallbacks();
	
	static DebugEventCallbacks * getObject(JNIEnv * env, jobject obj);
	
	// IUnknown.
	virtual ULONG __stdcall AddRef();
	virtual ULONG __stdcall Release();
    
    // IDebugEventCallbacks 
    virtual HRESULT __stdcall GetInterestMask(ULONG * Mask);
    
	virtual HRESULT __stdcall CreateProcess(
		ULONG64 ImageFileHandle,
		ULONG64 Handle,
		ULONG64 BaseOffset,
		ULONG ModuleSize,
		PCWSTR ModuleName,
		PCWSTR ImageName,
		ULONG CheckSum,
		ULONG TimeDateStamp,
		ULONG64 InitialThreadHandle,
		ULONG64 ThreadDataOffset,
		ULONG64 StartOffset);

	virtual HRESULT __stdcall ExitProcess(ULONG ExitCode);

};

#endif /*DEBUGEVENTCALLBACKS_H_*/
