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
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;

public class WinDbgRegisterManager {
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager#getRegisterObjects()
	 */
	public ICDIRegisterDescriptor[] getRegisterObjects() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager#createRegister(org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor)
	 */
	public ICDIRegister createRegister(ICDIRegisterDescriptor reg)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager#destroyRegister(org.eclipse.cdt.debug.core.cdi.model.ICDIRegister)
	 */
	public void destroyRegister(ICDIRegister reg) {
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
