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
package org.eclipse.cdt.debug.win32.core.cdi;

import java.util.Properties;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

public class WinDbgRuntimeOptions implements ICDIRuntimeOptions {

	private WinDbgTarget wTarget;

	/**
	 * 
	 */
	public WinDbgRuntimeOptions(WinDbgTarget target) {
		wTarget = target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions#setArguments(java.lang.String[])
	 */
	public void setArguments(String[] args) throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions#setEnvironment(java.util.Properties)
	 */
	public void setEnvironment(Properties props) throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions#setWorkingDirectory(java.lang.String)
	 */
	public void setWorkingDirectory(String wd) throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
	 */
	public ICDITarget getTarget() {
		return wTarget;
	}
}
