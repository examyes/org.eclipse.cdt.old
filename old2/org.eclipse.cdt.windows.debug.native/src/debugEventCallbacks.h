/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/
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

	virtual HRESULT __stdcall CreateThread(
        ULONG64 Handle,
        ULONG64 DataOffset,
        ULONG64 StartOffset);

	virtual HRESULT __stdcall ExitThread(ULONG ExitCode);

	virtual HRESULT __stdcall Breakpoint(IDebugBreakpoint2 * Bp);
	
};

#endif /*DEBUGEVENTCALLBACKS_H_*/
