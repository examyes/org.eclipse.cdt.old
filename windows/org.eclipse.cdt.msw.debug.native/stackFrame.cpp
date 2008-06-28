#include <jni.h>
#include <dbgeng.h>
#include "native.h"

#define NATIVE(ret, func) extern "C" JNIEXPORT ret JNICALL Java_org_eclipse_cdt_msw_debug_dbgeng_DebugStackFrame_##func

NATIVE(jlong, nativeInit)(JNIEnv * env, jclass cls) {
	return (jlong)new DEBUG_STACK_FRAME[FRAME_CHUNK_SIZE];
}

NATIVE(void, nativeDelete)(JNIEnv * env, jclass cls, jlong object) {
	delete[] (DEBUG_STACK_FRAME *)object;
}

NATIVE(jlong, nativeGetInstructionOffset)(JNIEnv * env, jclass cls, jlong object, jint index) {
	DEBUG_STACK_FRAME * stackFrame = ((DEBUG_STACK_FRAME *)object) + index;
	return stackFrame->InstructionOffset;
}

NATIVE(jlong, nativeGetReturnOffset)(JNIEnv * env, jclass cls, jlong object, jint index) {
	DEBUG_STACK_FRAME * stackFrame = ((DEBUG_STACK_FRAME *)object) + index;
	return stackFrame->ReturnOffset;
}

NATIVE(jlong, nativeGetFrameOffset)(JNIEnv * env, jclass cls, jlong object, jint index) {
	DEBUG_STACK_FRAME * stackFrame = ((DEBUG_STACK_FRAME *)object) + index;
	return stackFrame->FrameOffset;
}

NATIVE(jlong,nativeGetStackOffset)(JNIEnv * env, jclass cls, jlong object, jint index) {
	DEBUG_STACK_FRAME * stackFrame = ((DEBUG_STACK_FRAME *)object) + index;
	return stackFrame->StackOffset;
}

NATIVE(jlong, nativeGetFuncTableEntry)(JNIEnv * env, jclass cls, jlong object, jint index) {
	DEBUG_STACK_FRAME * stackFrame = ((DEBUG_STACK_FRAME *)object) + index;
	return stackFrame->FuncTableEntry;
}

NATIVE(void, nativeGetParams)(JNIEnv * env, jclass cls, jlong object, jint index, jlongArray params) {
	DEBUG_STACK_FRAME * stackFrame = ((DEBUG_STACK_FRAME *)object) + index;
	env->SetLongArrayRegion(params, 0, 4, (jlong *)stackFrame->Params);
}

NATIVE(jboolean, nativeGetVirtual)(JNIEnv * env, jclass cls, jlong object, jint index) {
	DEBUG_STACK_FRAME * stackFrame = ((DEBUG_STACK_FRAME *)object) + index;
	return stackFrame->Virtual ? JNI_TRUE : JNI_FALSE;
}

NATIVE(jint, nativeGetFrameNumber)(JNIEnv * env, jclass cls, jlong object, jint index) {
	DEBUG_STACK_FRAME * stackFrame = ((DEBUG_STACK_FRAME *)object) + index;
	return stackFrame->FrameNumber;
}
