/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
package org.eclipse.cdt.debug.gdbPicl.objects;

import org.eclipse.cdt.debug.gdbPicl.DebugSession;

import com.ibm.debug.epdc.ERepGetNextBkp;

public class LoadBreakpoint extends Breakpoint {

	/**
	 * Constructor for LoadBreakpoint.
	 * @param debugSession
	 * @param bkpID
	 * @param gdbBkID
	 * @param bkpType
	 * @param bkpAttr
	 */
	public LoadBreakpoint(
		DebugSession debugSession,
		int bkpID,
		int gdbBkID,
		int bkpType,
		int bkpAttr,
		String dllName) {
		super(debugSession, bkpID, gdbBkID, bkpType, bkpAttr);
		_dllName = dllName;
	}

	/**
	 * @see Breakpoint#fillBreakpointChangeItem(ERepGetNextBkp)
	 */
	void fillBreakpointChangeItem(ERepGetNextBkp bkpChangeItem) {

		int _partID = 1;
		int _lineNum = 1;

		bkpChangeItem.setDU(0);
		bkpChangeItem.setVarInfo(_dllName);
		bkpChangeItem.setBkpContext((short) Part.VIEW_SOURCE,(short) _partID,1,_lineNum);
		bkpChangeItem.setBkpContext((short) Part.VIEW_DISASSEMBLY,(short) _partID,1,_lineNum);

		if (Part.MIXED_VIEW_ENABLED)
			bkpChangeItem.setBkpContext((short) Part.VIEW_MIXED, (short) _partID, 1, 1);
	}

	/**
	 * Gets the _dllName.
	 * @return Returns a String
	 */
	public String getDllName() {
		return _dllName;
	}

	/**
	 * Sets the _dllName.
	 * @param _dllName The _dllName to set
	 */
	public void setDllName(String _dllName) {
		this._dllName = _dllName;
	}

	private String _dllName;

}
