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
public class DebugSymbolEntry {

	private final long moduleBase;
	private final long offset;
	private final long id;
	private final long arg64;
	private final int size;
	private final int flags;
	private final int typeId;
	private final int nameSize;
	private final int token;
	private final int tag;
	private final int arg32;

	public DebugSymbolEntry(
			long moduleBase,
			long offset,
			long id,
			long arg64,
			int size,
			int flags,
			int typeId,
			int nameSize,
			int token,
			int tag,
			int arg32) {
		this.moduleBase = moduleBase;
		this.offset = offset;
		this.id = id;
		this.arg64 = arg64;
		this.size = size;
		this.flags = flags;
		this.typeId = typeId;
		this.nameSize = nameSize;
		this.token = token;
		this.tag = tag;
		this.arg32 = arg32;
	}

	public long getModuleBase() {
		return moduleBase;
	}

	public long getOffset() {
		return offset;
	}

	public long getId() {
		return id;
	}

	public long getArg64() {
		return arg64;
	}

	public int getSize() {
		return size;
	}

	public int getFlags() {
		return flags;
	}

	public int getTypeId() {
		return typeId;
	}

	public int getNameSize() {
		return nameSize;
	}

	public int getToken() {
		return token;
	}

	public int getTag() {
		return tag;
	}

	public int getArg32() {
		return arg32;
	}

}
