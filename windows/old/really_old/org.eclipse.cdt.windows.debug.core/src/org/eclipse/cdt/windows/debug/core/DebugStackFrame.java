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
public class DebugStackFrame {

	private long instructionOffset;
	private long returnOffset;
	private long frameOffset;
	private long stackOffset;
	private long funcTableEntry;
	private long[] params;
	private boolean virtual;
	private int frameNumber;

	@SuppressWarnings("unused")
	private DebugStackFrame(
			long instructionOffset,
			long returnOffset,
			long frameOffset,
			long stackOffset,
			long funcTableEntry,
			long[] params,
			boolean virtual,
			int frameNumber) {
		this.instructionOffset = instructionOffset;
		this.returnOffset = returnOffset;
		this.frameOffset = frameOffset;
		this.stackOffset = stackOffset;
		this.funcTableEntry = funcTableEntry;
		this.params = params;
		this.virtual = virtual;
		this.frameNumber = frameNumber;
	}

	public long getInstructionOffset() {
		return instructionOffset;
	}

	public long getReturnOffset() {
		return returnOffset;
	}

	public long getFrameOffset() {
		return frameOffset;
	}

	public long getStackOffset() {
		return stackOffset;
	}

	public long getFuncTableEntry() {
		return funcTableEntry;
	}

	public long[] getParams() {
		return params;
	}

	public boolean isVirtual() {
		return virtual;
	}

	public int getFrameNumber() {
		return frameNumber;
	}
	
}
