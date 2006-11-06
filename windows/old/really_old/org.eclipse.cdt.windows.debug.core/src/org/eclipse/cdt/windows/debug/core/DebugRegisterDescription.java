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
public class DebugRegisterDescription {

	private int type;
	private int flags;
	private int subregMaster;
	private int subregLength;
	private long subregMask;
	private int subregShift;

	public DebugRegisterDescription(
			int type,
			int flags,
			int subregMaster,
			int subregLength,
			long subregMask,
			int subregShift) {
		this.type = type;
		this.flags = flags;
		this.subregMaster = subregMaster;
		this.subregLength = subregLength;
		this.subregMask = subregMask;
		this.subregShift = subregShift;
	}

	public int getType() {
		return type;
	}

	public int getFlags() {
		return flags;
	}

	public int getSubregMaster() {
		return subregMaster;
	}

	public int getSubregLength() {
		return subregLength;
	}

	public long getSubregMask() {
		return subregMask;
	}

	public int getSubregShift() {
		return subregShift;
	}

}
