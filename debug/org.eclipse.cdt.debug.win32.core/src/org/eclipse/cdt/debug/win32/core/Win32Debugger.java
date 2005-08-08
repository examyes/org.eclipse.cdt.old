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

import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.ICDIDebugger;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.win32.core.cdi.WinDbgSession;
import org.eclipse.cdt.debug.win32.core.dbgeng.WinDbgEngine;
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
	
	static {
		try {
			CorePlugin.getDefault().loadLibrary("win32cdi");
		} catch (IOException e) {
			// This will manifest itself as a unsatisfied link exception later
		}
	}

	private static WinDbgEngine debugEngine;
	
	public static WinDbgEngine getDebugEngine() {
		if (debugEngine == null) {
			debugEngine = new WinDbgEngine();
			debugEngine.start();
		}
		
		return debugEngine;
	}

}
