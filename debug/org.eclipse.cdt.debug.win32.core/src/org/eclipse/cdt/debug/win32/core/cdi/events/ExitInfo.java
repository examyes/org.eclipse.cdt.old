/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.debug.win32.core.cdi.events;

import org.eclipse.cdt.debug.core.cdi.ICDIExitInfo;
import org.eclipse.cdt.debug.core.cdi.ICDISession;

public class ExitInfo implements ICDIExitInfo {

	private ICDISession session;
	private int exitCode;
	
	public ExitInfo(ICDISession session, int exitCode) {
		this.session = session;
		this.exitCode = exitCode;
	}
	
	public int getCode() {
		return exitCode;
	}

	public ICDISession getSession() {
		return session;
	}
}
