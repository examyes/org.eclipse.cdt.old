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
public class DebugSymbolSourceEntry {

	private final long moduleBase;
	private final long offset;
	private final long fileNameId;
	private final long engineInternal;
	private final int size;
	private final int flags;
	private final int fileNameSize;
	private final int startLine;
	private final int endLine;
	private final int startColumn;
	private final int endColumn;

	public DebugSymbolSourceEntry(
			long moduleBase,
			long offset,
			long fileNameId,
			long engineInternal,
			int size,
			int flags,
			int fileNameSize,
			int startLine,
			int endLine,
			int startColumn,
			int endColumn) {
		this.moduleBase = moduleBase;
		this.offset = offset;
		this.fileNameId = fileNameId;
		this.engineInternal = engineInternal;
		this.size = size;
		this.flags = flags;
		this.fileNameSize = fileNameSize;
		this.startLine = startLine;
		this.endLine = endLine;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	public long getModuleBase() {
		return moduleBase;
	}

	public long getOffset() {
		return offset;
	}

	public long getFileNameId() {
		return fileNameId;
	}

	public long getEngineInternal() {
		return engineInternal;
	}

	public int getSize() {
		return size;
	}

	public int getFlags() {
		return flags;
	}

	public int getFileNameSize() {
		return fileNameSize;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getStartColumn() {
		return startColumn;
	}

	public int getEndColumn() {
		return endColumn;
	}
	
}
