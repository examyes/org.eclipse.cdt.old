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

import org.eclipse.cdt.windows.debug.core.HRESULT;
import org.eclipse.cdt.windows.debug.core.IDebugBreakpoint;
import org.eclipse.cdt.windows.debug.core.IDebugControl;

/**
 * @author Doug Schaefer
 *
 */
public class SetBreakpointCommand extends DebugCommand {
	
	private final String expression;
	private IDebugBreakpoint bp;
	
	public SetBreakpointCommand(String expression) {
		this.expression = expression;
	}
	
	public IDebugBreakpoint getBreakpoint() {
		return bp;
	}
	
	@Override
	public int run(DebugEngine engine) {
		IDebugControl control = engine.getDebugControl();
		bp = new IDebugBreakpoint();
		int hr = control.addBreakpoint(
				IDebugControl.DEBUG_BREAKPOINT_CODE,
				IDebugControl.DEBUG_ANY_ID, bp);
		if (HRESULT.FAILED(hr))
			return hr;
		bp.setOffsetExpression(expression);
		bp.addFlags(IDebugBreakpoint.DEBUG_BREAKPOINT_ENABLED);
		return HRESULT.S_OK;
	}

}
