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
import org.eclipse.cdt.windows.debug.core.IDebugControl;

/**
 * @author Doug Schaefer
 *
 */
public class ResumeCommand extends DebugCommand {

	@Override
	protected String getName() {
		return "Resume";
	}
	
	@Override
	public int run(DebugEngine engine) {
		IDebugControl control = engine.getDebugControl();
		int hr = control.setExecutionStatus(IDebugControl.DEBUG_STATUS_GO);
		if (HRESULT.FAILED(hr))
			return hr;
		return control.waitForEvent(0, IDebugControl.INFINITE);
	}

}
