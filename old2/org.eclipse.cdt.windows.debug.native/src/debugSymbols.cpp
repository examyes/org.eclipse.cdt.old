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
#include <windows.h>
#include <jni.h>
#include <dbgeng.h>

#include "util.h"

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugSymbols_## name
#define JNISTDMETHOD(name, ...) extern "C" JNIEXPORT jint JNINAME(name)(JNIEnv * env, jobject obj, __VA_ARGS__ ) { \
	try { IDebugSymbols3 * symbols = getObject(env, obj);
#define JNISTDEND } catch (jobject e) { env->Throw((jthrowable)e); return E_FAIL; } }

static jfieldID pID = NULL;

static jfieldID getPID(JNIEnv * env, jobject obj) {
	if (pID == NULL) {
		jclass cls = env->GetObjectClass(obj);
		pID = env->GetFieldID(cls, "p", "J");
		checkNull(env, pID);
	}
	return pID;
}

static IDebugSymbols3 * getObject(JNIEnv * env, jobject obj) {
	IDebugSymbols3 * symbols = (IDebugSymbols3 *)env->GetLongField(obj, getPID(env, obj));
	checkNull(env, symbols);
	return symbols;
}

void setObject(JNIEnv * env, jobject obj, IDebugSymbols3 * debugControl) {
	env->SetLongField(obj, getPID(env, obj), (jlong)debugControl);
}

// IDebugSymbols.

//	public native int getSymbolOptions(DebugInt options);
//	public native int addSymbolOptions(int options);
//	public native int removeSymbolOptions(int options);
//	public native int setSymbolOptions(int options);

//	public native int getNameByOffset(long offset, DebugString name,
//			DebugLong displacement);
JNISTDMETHOD(getNameByOffset, jlong offset, jobject name, jobject displacement)
	ULONG nameSize;
	HRESULT hr = symbols->GetNameByOffsetWide(offset, NULL, 0, &nameSize, NULL);
	if (FAILED(hr))
		return E_FAIL;
	wchar_t * _name = new wchar_t[nameSize];
	ULONG64 _displacement;
	hr = symbols->GetNameByOffsetWide(offset, _name, nameSize, NULL,
			displacement != NULL ? &_displacement : NULL);
	if (!FAILED(hr)) {
		setObject(env, name, _name);
		if (displacement != NULL)
			setObject(env, displacement, (jlong)_displacement);
	}
	delete[] _name;
	return S_OK;
JNISTDEND

//	public native int getOffsetByName(String symbol, DebugLong offset);
//	public native int getNearNameByOffset(long offset, int delta,
//			DebugString name, DebugLong displacement);

//	public native int getLineByOffset(long offset,
//			DebugInt line, DebugString file, DebugLong displacement);
JNISTDMETHOD(getLineByOffset, jlong offset, jobject line, jobject file,
		jobject displacement)
	ULONG _fileSize;
	HRESULT hr = symbols->GetLineByOffsetWide(offset, NULL, NULL, 0, &_fileSize, NULL);
	if (FAILED(hr))
		return hr;
	ULONG _line;
	wchar_t * _file = new wchar_t[_fileSize];
	ULONG64 _displacement;
	hr = symbols->GetLineByOffsetWide(offset,
			line != NULL ? &_line : NULL,
			_file, _fileSize, NULL,
			displacement != NULL ? &_displacement : NULL);
	if (!FAILED(hr)) {
		if (line != NULL)
			setObject(env, line, (jint)_line);
		setObject(env, file, _file);
		if (displacement != NULL)
			setObject(env, displacement, (jlong)_displacement);
	}
	delete[] _file;
	return hr;
JNISTDEND

//	public native int getOffsetByLine(int line, String file, DebugLong offset);
//	public native int getNumberModules(DebugInt loaded, DebugInt unloaded);
//	public native int getModuleByIndex(int index, DebugLong base);
//	public native int getModuleByModuleName(String name, int startIndex,
//			DebugInt index, DebugLong base);
//	public native int getModuleByOffset(long offset, int startIndex,
//			DebugInt index, DebugLong base);
//	public native int getModuleNames(int index, long base,
//			DebugString imageName, DebugString moduleName,
//			DebugString loadedImageName);
//	public native int getModuleParameters(long[] bases, int start,
//			DebugModuleParameters[] params);
//	public native int getSymbolModule(String symbol, DebugLong base);
//	public native int getTypeName(long module, int typeId, DebugString name);
//	public native int getTypeId(long module, String name, DebugInt typeId);
//	public native int getTypeSize(long module, int typeId, DebugInt size);
//	public native int getFieldOffset(long module, int typeId, String field,
//			DebugInt offset);
//	public native int getSymbolTypeId(String symbol, int typeId,
//			DebugLong module);
//	public native int getOffsetTypeId(long offset, DebugInt typeId,
//			DebugLong module);
//	public native int readTypedDataVirtual(long offset, long module,
//			int typeId, byte[] buffer, DebugInt bytesRead);
//	public native int writeTypedDataVirtual(long offset, long module,
//			int typeid, byte[] buffer, DebugInt bytesWritten);
//	public native int outputTypedDataVirtual(int outputControl,
//			long offset, long module, int typeId, int flags);
//	public native int readTypedDataPhysical(long offset, long module,
//			int typeId, byte[] buffer, DebugInt bytesRead); 
//	public native int writeTypedDataPhysical(long offset, long module,
//			int typeId, byte[] buffer, DebugInt bytesWritten);
//	public native int outputTypedDataPhysical(int outputControl,
//			long offset, long module, int typeId, int flags);
//	public native int getScope(); // TODO 
//	public native int setScope(); // TODO
//	public native int resetScope();
//	public native int getScopeSymbolGroup(int flags,
//			IDebugSymbolGroup update, IDebugSymbolGroup symbols);
//	public native int createSymbolGroup(IDebugSymbolGroup group);
//	public native int startSymbolMatch(String Patter, DebugLong handle);
//	public native int getNextSymbolMatch(long handle,
//			DebugString match, DebugLong offset);
//	public native int endSymbolMatch(long handle);
//	public native int reload(String module);
//	public native int getSymbolPath(DebugString path);
//	public native int setSymbolPath(String path);
//	public native int appendSymbolPath(String addition);
//	public native int getImagePath(DebugString path);
//	public native int setImagePath(String path);
//	public native int appendImagePath(String addition);
//	public native int getSourcePath(DebugString path);
//	public native int getSourcePathElement(int index, DebugString element);
//	public native int setSourcePath(String path);
//	public native int appendSourcePath(String addition);
//	public native int findSourceFile(int startElement, String file,
//			int flags, DebugInt foundElement, DebugString found);
//	public native int getSourceFileLineOffsets(String file,
//			DebugLongArray lines);
//
//	 IDebugSymbols2.
//
//	public native int getModuleVersionInformation(int index, long base,
//			String item /*, TODO */);
//	public native int getModuleNameString(int which, int index,
//			long base, DebugString name);
//	public native int getConstantName(long module, int typeId, long value,
//			DebugString name);
//	public native int getFieldName(long module, int typeId, int fieldIndex,
//			DebugString name);
//	public native int getTypeOptions(DebugInt options);
//    public native int addTypeOptions(int options);
//    public native int removeTypeOptions(int options);
//    public native int setTypeOptions(int options);
//
//     IDebugSymbols3.
//
//    public native int isManagedModule(int index, long base);
//    public native int getModuleByModuleName2(String name, int startIndex, int flags,
//    		DebugInt index, DebugLong base);
//    public native int getModuleByOffset2(long offset, int startIndex, int flags,
//    		DebugInt index, DebugLong base);
//    public native int addSyntheticModule(long base, int size, String imagePath,
//    		String moduleName, int flags);
//    public native int removeSyntheticModule(long base);
//    public native int getCurrentScopeFrameIndex(DebugLong index);
//    public native int setScopeFrameByIndex(long index);
//    public native int setScopeFromJitDebugInfo(int outputControl, long infoOffset);
//    public native int setScopeFromStoredEvent();
//    public native int outputSymbolByOffset(int outputControl, int flags, long offset);
//    public native int getFunctionEntryByOffset(long offset, int flags /*, TODO */);
//    public native int getFieldTypeAndOffset(long module, int containerTypeId,
//    		String field, DebugInt fieldTypeId, DebugInt offset);
//    public native int addSyntheticSymbol(long offset, int size, String name,
//    		int flags, DebugModuleAndId id);
//    public native int removeSyntheticSymbol(DebugModuleAndId id);
//    public native int getSymbolEntriesByOffset(long offset, int flags,
//    		DebugModuleAndId[] ids, long[] displacements, DebugInt entries);
//    public native int getSymbolEntriesByName(String symbol, int flags,
//    		DebugModuleAndId[] ids, DebugInt entries);
//    public native int getSymbolEntryByToken(long moduleBase, int token,
//    		DebugModuleAndId id);
//    public native int getSymbolEntryInformation(DebugModuleAndId id,
//    		DebugSymbolEntry info);
//    public native int getSymbolEntryString(DebugModuleAndId id, int which,
//    		DebugString string);
//    public native int getSymbolEntryOffsetRegions(DebugModuleAndId id,
//    		int flags, DebugOffsetRegions[] regions, DebugInt regionsAvail);
//    public native int getSymbolEntryBySymbolEntry(DebugModuleAndId fromId,
//    		int flags, DebugModuleAndId toId);
//    public native int getSourceEntriesByOffset(long offset, int flags,
//    		DebugSymbolSourceEntry[] entries, long entriesAvail);
//    public native int getSourceEntriesByLine(int line, String file,
//    		int flags, DebugSymbolSourceEntry[] entries, DebugInt entriesAvail);
//    public native int getSourceEntryString(DebugSymbolSourceEntry entry,
//    		int which, DebugString string);
//    public native int getSourceEntryOffsetRegions(DebugSymbolSourceEntry entry,
//    		int flags, DebugOffsetRegions[] regions, DebugInt regionsAvail);
//    public native int getSourceEntryBySourceEntry(DebugSymbolSourceEntry fromEntry,
//    		int flags, DebugSymbolSourceEntry toEntry);
