/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/

#include <DbgEng.h>
#include <jni.h>

// Utility should be moved
static JavaVM * javaVM = NULL;

static JNIEnv * attachCurrentThread() {
	if (javaVM == NULL)
		return NULL;
	
	JNIEnv * env = NULL;
	javaVM->AttachCurrentThread((void **)&env, NULL);
	return env;
}

// Bridge to IDebugEventCallbacks

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_dbgeng_IDebugEventCallbacks_##name

class DebugEventCallbacks : public IDebugEventCallbacks {
public:
	DebugEventCallbacks(JNIEnv * env, jobject _obj)
		: count(1)
	{
		obj = env->NewGlobalRef(_obj);
	}
	
	~DebugEventCallbacks() {
		if (obj != NULL) {
			JNIEnv * env = attachCurrentThread();
			if (env != NULL)
				env->DeleteGlobalRef(obj);
		}
	}
	
	// Called from Java to delete the peer java object
	void release(JNIEnv * env) {
		env->DeleteGlobalRef(obj);
		obj = NULL;
		Release();
	}
	
	// IUnknown
	
	HRESULT __stdcall QueryInterface(const IID & iid, PVOID * ref) {
		*ref = NULL;
		
		if (iid == __uuidof(IDebugEventCallbacks))
			*ref = this;
		else if (iid == __uuidof(IUnknown))
			*ref = static_cast<IUnknown *>(this);
		else
			return E_NOINTERFACE;
		
		return S_OK;
	}
	
	ULONG __stdcall AddRef(void) {
		return InterlockedIncrement(&count);
	}

	ULONG __stdcall Release(void) {
		ULONG c = InterlockedDecrement(&count);
		if (c == 0)
			delete this;
		return c;
	}
	
	// IDebugEventCallbacks
	
    HRESULT __stdcall GetInterestMask(PULONG mask) {
    	return E_NOTIMPL;
    }

    HRESULT __stdcall Breakpoint(PDEBUG_BREAKPOINT bp) {
    	return E_NOTIMPL;
    }

    HRESULT __stdcall Exception(PEXCEPTION_RECORD64 exception, ULONG firstChance) {
    	return E_NOTIMPL;
    }
    
    HRESULT __stdcall CreateThread(ULONG64 handle, ULONG64 dataOffset, ULONG64 startOffset) {
    	return E_NOTIMPL;
    }

	HRESULT __stdcall ExitThread(ULONG exitCode) {
		return E_NOTIMPL;
	}
	
	HRESULT __stdcall CreateProcess(
		ULONG64 imageFileHandle,
		ULONG64 handle,
		ULONG64 baseOffset,
		ULONG moduleSize,
		PCSTR moduleName,
		PCSTR imageName,
		ULONG checkSum,
		ULONG timeDateStamp,
		ULONG64 initialThreadHandle,
		ULONG64 threadDataOffset,
		ULONG64 startOffset)
	{
		return E_NOTIMPL;
	}

	HRESULT __stdcall ExitProcess(ULONG exitCode) {
		return E_NOTIMPL;
	}

	HRESULT __stdcall LoadModule(
		ULONG64 imageFileHandle,
		ULONG64 baseOffset,
		ULONG moduleSize,
		PCSTR moduleName,
		PCSTR imageName,
		ULONG checkSum,
		ULONG timeDateStamp)
	{
		return E_NOTIMPL;
	}

	HRESULT __stdcall UnloadModule(PCSTR imageBaseName, ULONG64 baseOffset) {
		return E_NOTIMPL;
	}

	HRESULT __stdcall SystemError(ULONG error, ULONG level) {
		return E_NOTIMPL;
	}

	HRESULT __stdcall SessionStatus(ULONG status) {
		return E_NOTIMPL;
	}

	HRESULT __stdcall ChangeDebuggeeState(ULONG flags, ULONG64 argument) {
		return E_NOTIMPL;
	}

	HRESULT __stdcall ChangeEngineState(ULONG flags, ULONG64 argument) {
		return E_NOTIMPL;
	}

	HRESULT __stdcall ChangeSymbolState(ULONG flags, ULONG64 argument) {
		return E_NOTIMPL;
	}

	LONG count;
	jobject obj;

	// methodIDs
	static jmethodID breakpointID;
	static jmethodID createProcessID;
	static jmethodID exitProcessID;
};

// private native long create();
NATIVE(jlong, create)(JNIEnv * env, jobject obj) {
	if (javaVM == NULL) {
		env->GetJavaVM(&javaVM);
		jclass cls = env->GetObjectClass(obj);
		// Get methodIDs
	}

	return (jlong)new DebugEventCallbacks(env, obj);
}

// private static native void release(long p);
NATIVE(void, release)(JNIEnv * env, jclass cls, jlong p) {
	DebugEventCallbacks * callbacks = (DebugEventCallbacks *)p;
	callbacks->release(env);
}
