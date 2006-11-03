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

package org.eclipse.cdt.windows.debug.core;

/**
 * @author Doug Schaefer
 *
 */
public class IDebugSymbols {

	@SuppressWarnings("unused")
	private long p;

	public static final int DEBUG_MODULE_LOADED				= 0x00000000;
	public static final int DEBUG_MODULE_UNLOADED			= 0x00000001;
	public static final int DEBUG_MODULE_USER_MODE			= 0x00000002;
	public static final int DEBUG_MODULE_EXPLICIT			= 0x00000008;
	public static final int DEBUG_MODULE_SECONDARY			= 0x00000010;
	public static final int DEBUG_MODULE_SYNTHETIC			= 0x00000020;
	public static final int DEBUG_MODULE_SYM_BAD_CHECKSUM	= 0x00010000;

	public static final int DEBUG_SYMTYPE_NONE		= 0;
	public static final int DEBUG_SYMTYPE_COFF		= 1;
	public static final int DEBUG_SYMTYPE_CODEVIEW	= 2;
	public static final int DEBUG_SYMTYPE_PDB		= 3;
	public static final int DEBUG_SYMTYPE_EXPORT	= 4;
	public static final int DEBUG_SYMTYPE_DEFERRED	= 5;
	public static final int DEBUG_SYMTYPE_SYM		= 6;
	public static final int DEBUG_SYMTYPE_DIA		= 7;

	public static final int DEBUG_SCOPE_GROUP_ARGUMENTS	= 0x00000001;
	public static final int DEBUG_SCOPE_GROUP_LOCALS	= 0x00000002;
	public static final int DEBUG_SCOPE_GROUP_ALL		= 0x00000003;

	public static final int DEBUG_OUTTYPE_DEFAULT			= 0x00000000;
	public static final int DEBUG_OUTTYPE_NO_INDENT			= 0x00000001;
	public static final int DEBUG_OUTTYPE_NO_OFFSET			= 0x00000002;
	public static final int DEBUG_OUTTYPE_VERBOSE			= 0x00000004;
	public static final int DEBUG_OUTTYPE_COMPACT_OUTPUT	= 0x00000008;
	public static final int DEBUG_OUTTYPE_RECURSION_LEVEL(int max) {
		return (((max) & 0xf) << 4);
	}
	public static final int DEBUG_OUTTYPE_ADDRESS_OF_FIELD	= 0x00010000;
	public static final int DEBUG_OUTTYPE_ADDRESS_AT_END	= 0x00020000;
	public static final int DEBUG_OUTTYPE_BLOCK_RECURSE		= 0x00200000;

	public static final int DEBUG_FIND_SOURCE_DEFAULT		= 0x00000000;
	public static final int DEBUG_FIND_SOURCE_FULL_PATH		= 0x00000001;
	public static final int DEBUG_FIND_SOURCE_BEST_MATCH	= 0x00000002;
	public static final int DEBUG_FIND_SOURCE_NO_SRCSRV		= 0x00000004;
	public static final int DEBUG_FIND_SOURCE_TOKEN_LOOKUP	= 0x00000008;

	public static final int DEBUG_INVALID_OFFSET = -1;
	
	public static final int DEBUG_MODNAME_IMAGE			= 0x00000000;
	public static final int DEBUG_MODNAME_MODULE		= 0x00000001;
	public static final int DEBUG_MODNAME_LOADED_IMAGE	= 0x00000002;
	public static final int DEBUG_MODNAME_SYMBOL_FILE	= 0x00000003;
	public static final int DEBUG_MODNAME_MAPPED_IMAGE	= 0x00000004;

	public static final int DEBUG_TYPEOPTS_UNICODE_DISPLAY		= 0x00000001;
	public static final int DEBUG_TYPEOPTS_LONGSTATUS_DISPLAY	= 0x00000002;
	public static final int DEBUG_TYPEOPTS_FORCERADIX_OUTPUT	= 0x00000004;
	public static final int DEBUG_TYPEOPTS_MATCH_MAXSIZE		= 0x00000008;

	public static final int DEBUG_GETMOD_DEFAULT				= 0x00000000;
	public static final int DEBUG_GETMOD_NO_LOADED_MODULES		= 0x00000001;
	public static final int DEBUG_GETMOD_NO_UNLOADED_MODULES	= 0x00000002;

	public static final int DEBUG_ADDSYNTHMOD_DEFAULT = 0x00000000;
	
	public static final int DEBUG_ADDSYNTHSYM_DEFAULT	= 0x00000000;

	public static final int DEBUG_OUTSYM_DEFAULT			= 0x00000000;
	public static final int DEBUG_OUTSYM_FORCE_OFFSET		= 0x00000001;
	public static final int DEBUG_OUTSYM_SOURCE_LINE		= 0x00000002;
	public static final int DEBUG_OUTSYM_ALLOW_DISPLACEMENT	= 0x00000004;

	public static final int DEBUG_GETFNENT_DEFAULT			= 0x00000000;
	public static final int DEBUG_GETFNENT_RAW_ENTRY_ONLY	= 0x00000001;

	public static final int DEBUG_SOURCE_IS_STATEMENT	= 0x00000001;

	public static final int DEBUG_GSEL_DEFAULT			= 0x00000000;
	public static final int DEBUG_GSEL_NO_SYMBOL_LOADS	= 0x00000001;
	public static final int DEBUG_GSEL_ALLOW_LOWER		= 0x00000002;
	public static final int DEBUG_GSEL_ALLOW_HIGHER		= 0x00000004;
	public static final int DEBUG_GSEL_NEAREST_ONLY		= 0x00000008;

    // IDebugSymbols.

	public native int getSymbolOptions(DebugInt options);
	public native int addSymbolOptions(int options);
	public native int removeSymbolOptions(int options);
	public native int setSymbolOptions(int options);
	public native int getNameByOffset(long offset, DebugString name,
			DebugLong displacement);
	public native int getOffsetByName(String symbol, DebugLong offset);
	public native int getNearNameByOffset(long offset, int delta,
			DebugString name, DebugLong displacement);
	public native int getLineByOffset(long offset,
			DebugInt line, DebugString file, DebugLong displacement);
	public native int getOffsetByLine(int line, String file, DebugLong offset);
	public native int getNumberModules(DebugInt loaded, DebugInt unloaded);
	public native int getModuleByIndex(int index, DebugLong base);
	public native int getModuleByModuleName(String name, int startIndex,
			DebugInt index, DebugLong base);
	public native int getModuleByOffset(long offset, int startIndex,
			DebugInt index, DebugLong base);
	public native int getModuleNames(int index, long base,
			DebugString imageName, DebugString moduleName,
			DebugString loadedImageName);
	public native int getModuleParameters(long[] bases, int start,
			DebugModuleParameters[] params);
	public native int getSymbolModule(String symbol, DebugLong base);
	public native int getTypeName(long module, int typeId, DebugString name);
	public native int getTypeId(long module, String name, DebugInt typeId);
	public native int getTypeSize(long module, int typeId, DebugInt size);
	public native int getFieldOffset(long module, int typeId, String field,
			DebugInt offset);
	public native int getSymbolTypeId(String symbol, int typeId,
			DebugLong module);
	public native int getOffsetTypeId(long offset, DebugInt typeId,
			DebugLong module);
	public native int readTypedDataVirtual(long offset, long module,
			int typeId, byte[] buffer, DebugInt bytesRead);
	public native int writeTypedDataVirtual(long offset, long module,
			int typeid, byte[] buffer, DebugInt bytesWritten);
	public native int outputTypedDataVirtual(int outputControl,
			long offset, long module, int typeId, int flags);
	public native int readTypedDataPhysical(long offset, long module,
			int typeId, byte[] buffer, DebugInt bytesRead); 
	public native int writeTypedDataPhysical(long offset, long module,
			int typeId, byte[] buffer, DebugInt bytesWritten);
	public native int outputTypedDataPhysical(int outputControl,
			long offset, long module, int typeId, int flags);
	public native int getScope(); // TODO 
	public native int setScope(); // TODO
	public native int resetScope();
	public native int getScopeSymbolGroup(int flags,
			IDebugSymbolGroup update, IDebugSymbolGroup symbols);
	public native int createSymbolGroup(IDebugSymbolGroup group);
	public native int startSymbolMatch(String Patter, DebugLong handle);
	public native int getNextSymbolMatch(long handle,
			DebugString match, DebugLong offset);
	public native int endSymbolMatch(long handle);
	public native int reload(String module);
	public native int getSymbolPath(DebugString path);
	public native int setSymbolPath(String path);
	public native int appendSymbolPath(String addition);
	public native int getImagePath(DebugString path);
	public native int setImagePath(String path);
	public native int appendImagePath(String addition);
	public native int getSourcePath(DebugString path);
	public native int getSourcePathElement(int index, DebugString element);
	public native int setSourcePath(String path);
	public native int appendSourcePath(String addition);
	public native int findSourceFile(int startElement, String file,
			int flags, DebugInt foundElement, DebugString found);
	public native int getSourceFileLineOffsets(String file,
			DebugLongArray lines);

	// IDebugSymbols2.

	public native int getModuleVersionInformation(int index, long base,
			String item /*, TODO */);
	public native int getModuleNameString(int which, int index,
			long base, DebugString name);
	public native int getConstantName(long module, int typeId, long value,
			DebugString name);
	public native int getFieldName(long module, int typeId, int fieldIndex,
			DebugString name);
	public native int getTypeOptions(DebugInt options);
    public native int addTypeOptions(int options);
    public native int removeTypeOptions(int options);
    public native int setTypeOptions(int options);

    // IDebugSymbols3.

    public native int isManagedModule(int index, long base);
    public native int getModuleByModuleName2(String name, int startIndex, int flags,
    		DebugInt index, DebugLong base);
    public native int getModuleByOffset2(long offset, int startIndex, int flags,
    		DebugInt index, DebugLong base);
    public native int addSyntheticModule(long base, int size, String imagePath,
    		String moduleName, int flags);
    public native int removeSyntheticModule(long base);
    public native int getCurrentScopeFrameIndex(DebugLong index);
    public native int setScopeFrameByIndex(long index);
    public native int setScopeFromJitDebugInfo(int outputControl, long infoOffset);
    public native int setScopeFromStoredEvent();
    public native int outputSymbolByOffset(int outputControl, int flags, long offset);
    public native int getFunctionEntryByOffset(long offset, int flags /*, TODO */);
    public native int getFieldTypeAndOffset(long module, int containerTypeId,
    		String field, DebugInt fieldTypeId, DebugInt offset);
    public native int addSyntheticSymbol(long offset, int size, String name,
    		int flags, DebugModuleAndId id);
    public native int removeSyntheticSymbol(DebugModuleAndId id);
    public native int getSymbolEntriesByOffset(long offset, int flags,
    		DebugModuleAndId[] ids, long[] displacements, DebugInt entries);
    public native int getSymbolEntriesByName(String symbol, int flags,
    		DebugModuleAndId[] ids, DebugInt entries);
    public native int getSymbolEntryByToken(long moduleBase, int token,
    		DebugModuleAndId id);
    public native int getSymbolEntryInformation(DebugModuleAndId id,
    		DebugSymbolEntry info);
    public native int getSymbolEntryString(DebugModuleAndId id, int which,
    		DebugString string);
    public native int getSymbolEntryOffsetRegions(DebugModuleAndId id,
    		int flags, DebugOffsetRegions[] regions, DebugInt regionsAvail);
    public native int getSymbolEntryBySymbolEntry(DebugModuleAndId fromId,
    		int flags, DebugModuleAndId toId);
    public native int getSourceEntriesByOffset(long offset, int flags,
    		DebugSymbolSourceEntry[] entries, long entriesAvail);
    public native int getSourceEntriesByLine(int line, String file,
    		int flags, DebugSymbolSourceEntry[] entries, DebugInt entriesAvail);
    public native int getSourceEntryString(DebugSymbolSourceEntry entry,
    		int which, DebugString string);
    public native int getSourceEntryOffsetRegions(DebugSymbolSourceEntry entry,
    		int flags, DebugOffsetRegions[] regions, DebugInt regionsAvail);
    public native int getSourceEntryBySourceEntry(DebugSymbolSourceEntry fromEntry,
    		int flags, DebugSymbolSourceEntry toEntry);

}
