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
 */
public class IDebugRegisters {
	
	@SuppressWarnings("unused")
	private long p;
	
	public static final int DEBUG_REGISTERS_DEFAULT	= 0x00000000;
	public static final int DEBUG_REGISTERS_INT32		= 0x00000001;
	public static final int DEBUG_REGISTERS_INT64		= 0x00000002;
	public static final int DEBUG_REGISTERS_FLOAT		= 0x00000004;
	public static final int DEBUG_REGISTERS_ALL		= 0x00000007;

	public static final int DEBUG_REGISTER_SUB_REGISTER	= 0x00000001;

	public static final int DEBUG_REGSRC_DEBUGGEE	= 0x00000000;
	public static final int DEBUG_REGSRC_EXPLICIT	= 0x00000001;
	public static final int DEBUG_REGSRC_FRAME		= 0x00000002;

    // IDebugRegisters.

	public native int getNumberRegisters(DebugInt number);
	public native int getDescription(int register, DebugString name,
			DebugRegisterDescription desc);
	public native int getIndexByName(String name, DebugInt index);
	public native int getValue(int register, DebugValue value);
	public native int setValue(int register, DebugValue value);
	public native int getValues(int[] indices, int start, DebugValue[] values);
	public native int setValues(int[] indices, int start, DebugValue[] values);
	public native int outputRegisters(int outputControl, int flags);
	public native int getInstructionOffset(DebugLong offset);
	public native int getStackOffset(DebugLong offset);
	public native int getFrameOffset(DebugLong offset);

    // IDebugRegisters2.

	public native int getNumberPseudoRegisters(DebugInt number);
	public native int getPseudoDescription(int register, DebugString name,
			DebugLong typeModule, DebugInt typeId);
	public native int getPseudoIndexByName(String name, DebugInt index);
	public native int getPseudoValues(int source, int[] indices, int start,
			DebugValue[] values);
	public native int setPseudoValues(int source, int[] indices, int start,
			DebugValue[] values);
	public native int getValues2(int source, int[] indices, int start,
			DebugValue[] values);
	public native int setValues2(int source, int[] indices, int start,
			DebugValue[] values);
	public native int outputRegisters2(int outputControl, int source, int flags);
	public native int getInstructionOffset2(int source, DebugLong offset);
	public native int getStackOffset2(int source, DebugLong offset);
	public native int getFrameOffset2(int source, DebugLong offset);

}
