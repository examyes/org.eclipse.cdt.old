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

import org.eclipse.cdt.debug.core.ICDebugger;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.win32.core.cdi.WinDbgSession;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 */
public class Win32Debugger implements ICDebugger {
	/**
	 */
	public Win32Debugger() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICDebugger#createAttachSession(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.IFile, int)
	 */
	public ICDISession createAttachSession(ILaunchConfiguration config,
		IFile exe, int pid) throws CDIException
	{
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICDebugger#createCoreSession(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IPath)
	 */
	public ICDISession createCoreSession(ILaunchConfiguration config,
		IFile exe, IPath corefile) throws CDIException
	{
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICDebugger#createLaunchSession(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.resources.IFile)
	 */
	public ICDISession createLaunchSession(ILaunchConfiguration config,
		IFile exe) throws CDIException
	{
		return new WinDbgSession(config, exe);
	}
}
