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

import org.eclipse.cdt.windows.debug.core.IDebugBreakpoint;
import org.eclipse.cdt.windows.debug.core.IDebugEventCallbacks;
import org.eclipse.core.runtime.CoreException;

public class EventCallbacks extends IDebugEventCallbacks {
	
	private final DebugEngine engine;
	
	public EventCallbacks(DebugEngine engine) throws CoreException {
		super();
		this.engine = engine;
	}

	@Override
	protected int getInterestMask() {
		return	DEBUG_EVENT_CREATE_PROCESS |
				DEBUG_EVENT_BREAKPOINT;
	}

	@Override
	protected int createProcess(long imageFileHandle, long handle,
			long baseOffset, int moduleSize, String moduleName,
			String imageName, int checkSum, int timeDateStamp,
			long initialThreadHandle, long threadDataOffset, long startOffset) {
		engine.processStarted();
		return DEBUG_STATUS_BREAK;
	}
	
	@Override
	protected int breakpoint(IDebugBreakpoint bp) {
		return DEBUG_STATUS_BREAK;
	}
	
}
