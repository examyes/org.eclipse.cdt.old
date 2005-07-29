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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorage;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorageDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;

public class WinDbgVariableManager {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getGlobalVariableObject(java.lang.String, java.lang.String, java.lang.String)
	 */
	public ICDIGlobalVariableDescriptor getGlobalVariableDescriptor(WinDbgTarget wTarget, String filename,
			String function, String name) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIVariableDescriptor getVariableDecriptorAsArray(ICDIVariableDescriptor var,
			int start, int length) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIVariableDescriptor getVariableDescriptorAsType(ICDIVariableDescriptor var,
			String type) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDILocalVariableDescriptor[] getLocalVariableDescriptor(WinDbgStackFrame stack)
			throws CDIException {
		return new ICDILocalVariableDescriptor[0];
	}

	public ICDIArgumentDescriptor[] getArgumentDescriptors(WinDbgStackFrame stack)
			throws CDIException {
		return new ICDIArgumentDescriptor[0];
	}

	public ICDIVariable createVariable(ICDIVariableDescriptor var)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDILocalVariable createLocalVariable(ICDILocalVariableDescriptor var)
		throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIArgument createArgument(ICDIArgumentDescriptor var)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIGlobalVariable createGlobalVariable(ICDIGlobalVariableDescriptor var) 
		throws CDIException {
		return null;
	}

	public ICDIThreadStorage createThreadStorage(ICDIThreadStorageDescriptor var) 
		throws CDIException {
		return null;
	}

	public void destroyVariable(ICDIVariable var) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void setAutoUpdate(boolean update) {
		// TODO Auto-generated method stub
	}

	public boolean isAutoUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	public void update() throws CDIException {
		// TODO Auto-generated method stub
	}

	public void handleDebugEvents(ICDIEvent[] event) {
		// TODO Auto-generated method stub
	}

	public ICDISession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

}
