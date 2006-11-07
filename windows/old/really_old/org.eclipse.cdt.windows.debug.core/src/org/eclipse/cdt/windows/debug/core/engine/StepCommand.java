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

package org.eclipse.cdt.windows.debug.core.engine;

import org.eclipse.cdt.windows.debug.core.DebugInt;
import org.eclipse.cdt.windows.debug.core.DebugLong;
import org.eclipse.cdt.windows.debug.core.DebugString;
import org.eclipse.cdt.windows.debug.core.HRESULT;
import org.eclipse.cdt.windows.debug.core.IDebugControl;
import org.eclipse.cdt.windows.debug.core.IDebugRegisters;
import org.eclipse.cdt.windows.debug.core.IDebugSymbols;

/**
 * @author Doug Schaefer
 *
 */
public abstract class StepCommand extends DebugCommand {

	protected abstract int getExecutionStatus();
	
	@Override
	public int run(DebugEngine engine) {
		IDebugControl control = engine.getDebugControl();
		IDebugSymbols symbols = engine.getDebugSymbols();
		IDebugRegisters registers = engine.getDebugRegisters();
		
		DebugLong _offset = new DebugLong();
		int hr = registers.getInstructionOffset(_offset);
		if (HRESULT.FAILED(hr))
			return hr;

		DebugString _file = new DebugString();
		DebugInt _line = new DebugInt();
		hr = symbols.getLineByOffset(_offset.getLong(), _line, _file, null);
		if (HRESULT.FAILED(hr))
			return hr;
		String file = _file.getString();
		int line = _line.getInt();
		
		hr = control.setExecutionStatus(getExecutionStatus());
		if (HRESULT.FAILED(hr))
			return hr;
		hr = control.waitForEvent(0, IDebugControl.INFINITE);
		while (hr == HRESULT.S_OK) {
			hr = registers.getInstructionOffset(_offset);
			if (engine.isDebug())
				System.out.println("WinDbg stepped " + String.format("%x", _offset.getLong()));
			if (HRESULT.FAILED(hr))
				return hr;
			hr = symbols.getLineByOffset(_offset.getLong(), _line, _file, null);
			if (HRESULT.FAILED(hr)) {
				// Not sure where we went, need to step return
				if (engine.isDebug())
					System.out.println("WinDbg stepping return while stepping over");
				engine.stepReturn(getExecutionStatus());
			} else if (line != _line.getInt() || !file.equals(_file.getString()))
				return hr;
			if (engine.isDebug())
				System.out.println("WinDbg step wait");
			hr = control.waitForEvent(0, IDebugControl.INFINITE);
		}
		return hr;
	}

}
