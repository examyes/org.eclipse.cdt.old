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
import org.eclipse.cdt.debug.core.cdi.ICDIVariableManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;

public class WinDbgVariableManager implements ICDIVariableManager {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getGlobalVariableObject(java.lang.String, java.lang.String, java.lang.String)
	 */
	public ICDIVariableObject getGlobalVariableObject(String filename,
			String function, String name) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getVariableObjectAsArray(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject, int, int)
	 */
	public ICDIVariableObject getVariableObjectAsArray(ICDIVariableObject var,
			int start, int length) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getVariableObjectAsType(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject, java.lang.String)
	 */
	public ICDIVariableObject getVariableObjectAsType(ICDIVariableObject var,
			String type) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getLocalVariableObjects(org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame)
	 */
	public ICDIVariableObject[] getLocalVariableObjects(ICDIStackFrame stack)
			throws CDIException {
		return stack.getLocalVariables();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getVariableObjects(org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame)
	 */
	public ICDIVariableObject[] getVariableObjects(ICDIStackFrame stack)
			throws CDIException {
		// TODO Auto-generated method stub
		return stack.getLocalVariables();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#createVariable(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject)
	 */
	public ICDIVariable createVariable(ICDIVariableObject var)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getArgumentObjects(org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame)
	 */
	public ICDIArgumentObject[] getArgumentObjects(ICDIStackFrame stack)
			throws CDIException {
		// TODO Auto-generated method stub
		return stack.getArguments();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#createArgument(org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentObject)
	 */
	public ICDIArgument createArgument(ICDIArgumentObject var)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#destroyVariable(org.eclipse.cdt.debug.core.cdi.model.ICDIVariable)
	 */
	public void destroyVariable(ICDIVariable var) throws CDIException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIManager#setAutoUpdate(boolean)
	 */
	public void setAutoUpdate(boolean update) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIManager#isAutoUpdate()
	 */
	public boolean isAutoUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIManager#update()
	 */
	public void update() throws CDIException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents(ICDIEvent[] event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISessionObject#getSession()
	 */
	public ICDISession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

}
