#include <jni.h>
#include <dbgeng.h>

class CDTDebugEventCallbacks : public DebugBaseEventCallbacksWide {
private:
	JavaVM * vm;
	jobject object;
	jmethodID getInterestMaskID;
	jmethodID breakpointID;
	jmethodID exceptionID;
	jmethodID createThreadID;
	jmethodID exitThreadID;
	jmethodID createProcessID;
	jmethodID exitProcessID;
	jmethodID loadModuleID;
	jmethodID unloadModuleID;
	jmethodID systemErrorID;
	jmethodID sessionStatusID;
	jmethodID changeDebuggeeStateID;
	jmethodID changeEngineStateID;
	jmethodID changeSymbolStateID;

public:
	CDTDebugEventCallbacks(JNIEnv * env, jobject _object) {
		env->GetJavaVM(&vm);
		object = env->NewGlobalRef(_object);

		jclass cls = env->GetObjectClass(object);
		getInterestMaskID = env->GetMethodID(cls, "getInterestMask", "()I");
		breakpointID = env->GetMethodID(cls, "nativeBreakpoint", "(J)I");
		exceptionID = env->GetMethodID(cls, "nativeException", "(JI)I");
		createThreadID = env->GetMethodID(cls, "createThread", "(JJJ)I");
		exitThreadID = env->GetMethodID(cls, "exitThread", "(I)I");
		createProcessID = env->GetMethodID(cls, "createProcess",
				"(JJJILjava/lang/String;Ljava/lang/String;IIJJJ)I");
		exitProcessID = env->GetMethodID(cls, "exitProcess", "(I)I");
		loadModuleID = env->GetMethodID(cls, "loadModule",
				"(JJILjava/lang/String;Ljava/lang/String;II)I");
		unloadModuleID = env->GetMethodID(cls, "unloadModule", "(Ljava/lang/String;J)I");
		systemErrorID = env->GetMethodID(cls, "systemError", "(II)I");
		sessionStatusID = env->GetMethodID(cls, "sessionStatus", "(I)I");
		changeDebuggeeStateID = env->GetMethodID(cls, "changeDebuggeeState", "(IJ)I");
		changeEngineStateID = env->GetMethodID(cls, "changeEngineState", "(IJ)I");
		changeSymbolStateID = env->GetMethodID(cls, "changeSymbolState", "(IJ)I");
	}

	~CDTDebugEventCallbacks() {
		getEnv()->DeleteGlobalRef(object);
	}

	JNIEnv * getEnv() {
		JNIEnv * env;
		vm->AttachCurrentThread((void **)&env, NULL);
		return env;
	}

	// We don't need to reference count since we keep a handle on this object in Java land
	// TODO maybe
    STDMETHOD_(ULONG, AddRef)() {
    	return 0;
    }

    STDMETHOD_(ULONG, Release)() {
    	return 0;
    }

    STDMETHOD(GetInterestMask)(PULONG mask) {
    	*mask = getEnv()->CallIntMethod(object, getInterestMaskID);
    	return S_OK;
    }

    STDMETHOD(Breakpoint)(PDEBUG_BREAKPOINT2 bp) {
    	return getEnv()->CallIntMethod(object, breakpointID, (jlong)bp);
    }

    STDMETHOD(Exception)(PEXCEPTION_RECORD64 exception, ULONG firstChance) {
    	return getEnv()->CallIntMethod(object, exceptionID, (jlong)exception, firstChance);
    }

    STDMETHOD(CreateThread)(ULONG64 handle, ULONG64 dataOffset, ULONG64 startOffset) {
    	return getEnv()->CallIntMethod(object, createThreadID, (jlong)handle, (jlong)dataOffset, (jlong)startOffset);
    }

    STDMETHOD(ExitThread)(ULONG exitCode) {
    	return getEnv()->CallIntMethod(object, exitThreadID, exitCode);
    }

    STDMETHOD(CreateProcess)(ULONG64 imageFileHandle, ULONG64 handle, ULONG64 baseOffset,
    		ULONG moduleSize, PCWSTR moduleName, PCWSTR imageName, ULONG checkSum, ULONG timeDateStamp,
    		ULONG64 initialThreadHandle, ULONG64 threadDataOffset, ULONG64 startOffset) {
    	JNIEnv * env = getEnv();
    	jstring moduleNameStr = env->NewString((const jchar *)moduleName, wcslen(moduleName));
    	jstring imageNameStr = env->NewString((const jchar *)imageName, wcslen(imageName));
    	return env->CallIntMethod(object, createProcessID, (jlong)imageFileHandle, (jlong)handle,
    			(jlong)baseOffset, moduleSize, moduleNameStr, imageNameStr, checkSum,
    			timeDateStamp, (jlong)initialThreadHandle, (jlong)threadDataOffset, (jlong)startOffset);
    }

    STDMETHOD(ExitProcess)(ULONG exitCode) {
    	return getEnv()->CallIntMethod(object, exitProcessID, exitCode);
    }

    STDMETHOD(LoadModule)(ULONG64 imageFileHandle, ULONG64 baseOffset, ULONG moduleSize,
    		PCWSTR moduleName, PCWSTR imageName, ULONG checkSum, ULONG timeDateStamp) {
    	JNIEnv * env = getEnv();
    	jstring moduleNameStr = env->NewString((const jchar *)moduleName, wcslen(moduleName));
    	jstring imageNameStr = env->NewString((const jchar *)imageName, wcslen(imageName));
    	return env->CallIntMethod(object, loadModuleID, (jlong)imageFileHandle, (jlong)baseOffset,
    			moduleSize, moduleNameStr, imageNameStr, checkSum, timeDateStamp);
    }

    STDMETHOD(UnloadModule)(PCWSTR imageBaseName, ULONG64 baseOffset) {
    	JNIEnv * env = getEnv();
    	jstring imageBaseNameStr = env->NewString((const jchar *)imageBaseName, wcslen(imageBaseName));
    	return env->CallIntMethod(object, unloadModuleID, imageBaseNameStr, (jlong)baseOffset);
    }

    STDMETHOD(SystemError)(ULONG error, ULONG level) {
    	return getEnv()->CallIntMethod(object, systemErrorID, error, level);
    }

    STDMETHOD(SessionStatus)(ULONG status) {
    	return getEnv()->CallIntMethod(object, sessionStatusID, status);
    }

    STDMETHOD(ChangeDebuggeeState)(ULONG flags, ULONG64 argument) {
    	return getEnv()->CallIntMethod(object, changeDebuggeeStateID, flags, (jlong)argument);
    }

    STDMETHOD(ChangeEngineState)(ULONG flags, ULONG64 argument) {
    	return getEnv()->CallIntMethod(object, changeEngineStateID, flags, argument);
    }

    STDMETHOD(ChangeSymbolState)(ULONG flags, ULONG64 argument) {
    	return getEnv()->CallIntMethod(object, changeSymbolStateID, flags, argument);
    }

};

#define NATIVE(ret, func) extern "C" JNIEXPORT ret JNICALL Java_org_eclipse_cdt_msw_debug_dbgeng_IDebugEventCallbacks_##func

NATIVE(jlong, nativeInit)(JNIEnv * env, jobject object) {
	return (jlong)new CDTDebugEventCallbacks(env, object);
}

NATIVE(void, nativeDelete)(JNIEnv * env, jclass cls, jlong object) {
	delete (CDTDebugEventCallbacks *)object;
}
