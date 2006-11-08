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

package org.eclipse.cdt.windows.debug.core.cdi;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.windows.debug.core.DebugInt;
import org.eclipse.cdt.windows.debug.core.DebugStackFrame;
import org.eclipse.cdt.windows.debug.core.DebugString;
import org.eclipse.cdt.windows.debug.core.HRESULT;
import org.eclipse.cdt.windows.debug.core.IDebugSymbols;
import org.eclipse.cdt.windows.debug.core.engine.DebugEngine;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIStackFrame implements ICDIStackFrame {

	private final WinCDITarget target;
	private final WinCDIThread thread;
	private final WinCDILocator locator;
	
	public WinCDIStackFrame(WinCDITarget target, WinCDIThread thread,
			DebugEngine engine, DebugStackFrame frame) {
		this.target = target;
		this.thread = thread;
		this.locator = new WinCDILocator();

		long offset = frame.getInstructionOffset();
		IDebugSymbols symbols = engine.getDebugSymbols();
		DebugString name = new DebugString();
		int hr = symbols.getNameByOffset(offset, name, null);
		if (!HRESULT.FAILED(hr))
			locator.setFunction(name.getString());
		else {
			locator.setFunction(HRESULT.getMessage(hr));
		}
		if (frame.getFrameNumber() > 0)
			// Entries higher up in the stack show the next instruction
			// to be executed, not the call instruction.
			offset -= 4;
		DebugInt _line = new DebugInt();
		DebugString _file = new DebugString();
		hr = symbols.getLineByOffset(offset, _line, _file, null);
		if (!HRESULT.FAILED(hr)) {
			locator.setFile(_file.getString());
			int line = _line.getInt();
			locator.setLineNumber(line);
			locator.setAddress(new BigInteger(String.valueOf(offset)));
		} else {
			locator.setFile("<unknown>");
			locator.setAddress(new BigInteger("0"));
		}
	}
	
	public ICDIArgument createArgument(ICDIArgumentDescriptor varDesc)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDILocalVariable createLocalVariable(
			ICDILocalVariableDescriptor varDesc) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean equals(ICDIStackFrame stackframe) {
		// TODO Auto-generated method stub
		return false;
	}

	public ICDIArgumentDescriptor[] getArgumentDescriptors()
			throws CDIException {
		// TODO
		return new ICDIArgumentDescriptor[0];
	}

	public int getLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ICDILocalVariableDescriptor[] getLocalVariableDescriptors()
			throws CDIException {
		// TODO Auto-generated method stub
		return new ICDILocalVariableDescriptor[0];
	}

	public ICDILocator getLocator() {
		return locator;
	}

	public ICDIThread getThread() {
		return thread;
	}

	public void stepReturn() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepReturn(ICDIValue value) throws CDIException {
		// TODO Auto-generated method stub

	}

	public ICDITarget getTarget() {
		return target;
	}

}
