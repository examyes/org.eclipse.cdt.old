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
package org.eclipse.cdt.debug.win32.core;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.ICDIDebugger;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.win32.core.cdi.WinDbgSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;

/**
 */
public class Win32Debugger implements ICDIDebugger {

	public ICDISession createDebuggerSession(ILaunch launch, IBinaryObject exe,
			IProgressMonitor monitor) throws CoreException {
		return new WinDbgSession(launch, exe);
	}
	
}
