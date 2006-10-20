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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class IDebugControl {
	
	@SuppressWarnings("unused")
	private long p;
	
	private native int init();

	public IDebugControl() throws CoreException {
		if (HRESULT.FAILED(init()))
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"Failed to init"));
	}
	
	public static final int INFINITE = 0xffffffff;

	public native int waitForEvent(int flags, int timeout);

	public static final int DEBUG_BREAKPOINT_CODE = 0;
	public static final int DEBUG_BREAKPOINT_DATA = 1;
	public static final int DEBUG_ANY_ID = 0xffffffff;
	
	public native int addBreakpoint(int type, int desiredId,
			IDebugBreakpoint[] bp);
	
}
