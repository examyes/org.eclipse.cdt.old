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
public class DebugModuleParameters {

	private long base;
	private int size;
	private int timeDateStamp;
	private int checksum;
	private int flags;
	private int symbolType;
	private int imageNameSize;
	private int moduleNameSize;
	private int loadedImageNameSize;
	private int symbolFileNameSize;
	private int mappedImageNameSize;

	public DebugModuleParameters(
			long base,
			int size,
			int timeDateStamp,
			int checksum,
			int flags,
			int symbolType,
			int imageNameSize,
			int moduleNameSize,
			int loadedImageNameSize,
			int symbolFileNameSize,
			int mappedImageNameSize) {
		this.base = base;
		this.size = size;
		this.timeDateStamp = timeDateStamp;
		this.checksum = checksum;
		this.flags = flags;
		this.symbolType = symbolType;
		this.imageNameSize = imageNameSize;
		this.moduleNameSize = moduleNameSize;
		this.loadedImageNameSize = loadedImageNameSize;
		this.symbolFileNameSize = symbolFileNameSize;
		this.mappedImageNameSize = mappedImageNameSize;
	}

	public long getBase() {
		return base;
	}

	public int getSize() {
		return size;
	}

	public int getTimeDateStamp() {
		return timeDateStamp;
	}

	public int getChecksum() {
		return checksum;
	}

	public int getFlags() {
		return flags;
	}

	public int getSymbolType() {
		return symbolType;
	}

	public int getImageNameSize() {
		return imageNameSize;
	}

	public int getModuleNameSize() {
		return moduleNameSize;
	}

	public int getLoadedImageNameSize() {
		return loadedImageNameSize;
	}

	public int getSymbolFileNameSize() {
		return symbolFileNameSize;
	}

	public int getMappedImageNameSize() {
		return mappedImageNameSize;
	}

}
