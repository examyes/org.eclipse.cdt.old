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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIStackFrame implements ICDIStackFrame {

	private final WinCDITarget target;
	private final WinCDIThread thread;
	private final ICDILocator locator;
	
	public WinCDIStackFrame(WinCDITarget target, WinCDIThread thread, ICDILocator locator) {
		this.target = target;
		this.thread = thread;
		this.locator = locator;
	}
	
	public ICDIArgument createArgument(ICDIArgumentDescriptor varDesc)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDILocalVariable createLocalVariable(
			ICDILocalVariableDescriptor varDesc) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean equals(ICDIStackFrame stackframe) {
		// TODO Auto-generated method stub
		return false;
	}

	public ICDIArgumentDescriptor[] getArgumentDescriptors()
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ICDILocalVariableDescriptor[] getLocalVariableDescriptors()
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDILocator getLocator() {
		return locator;
	}

	public ICDIThread getThread() {
		return thread;
	}

	public void stepReturn() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepReturn(ICDIValue value) throws CDIException {
		// TODO Auto-generated method stub

	}

	public ICDITarget getTarget() {
		return target;
	}

}
