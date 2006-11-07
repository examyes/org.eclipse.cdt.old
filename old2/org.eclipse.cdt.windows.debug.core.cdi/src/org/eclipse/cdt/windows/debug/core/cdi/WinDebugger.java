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

import java.io.File;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.ICDIDebugger2;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.windows.debug.core.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;

/**
 * @author Doug Schaefer
 *
 */
public class WinDebugger implements ICDIDebugger2 {

	public ICDISession createSession(ILaunch launch, File executable,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		try {
			return new WinCDISession(launch, executable);
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
			return null;
		}
	}

	public ICDISession createDebuggerSession(ILaunch launch, IBinaryObject exe,
			IProgressMonitor monitor) throws CoreException {
		// TODO find where the exe is located and create the File object
		return null;
	}
	
}
