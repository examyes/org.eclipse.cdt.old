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
	virtual HRESULT __stdcall QueryInterface(REFIID InterfaceId,
											 void ** Interface);
	virtual ULONG __stdcall AddRef();
	virtual ULONG __stdcall Release();
    
    // IDebugEventCallbacks 
    virtual HRESULT __stdcall GetInterestMask(ULONG * Mask);
};

#endif /*DEBUGEVENTCALLBACKS_H_*/
