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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;

public class WinDbgStackFrame implements ICDIStackFrame {

	private ICDILocation location;
	private WinDbgThread thread;
	private int level = 0;
	private long pc;
	private long frame;
	private boolean populated = false;
	private List arguments = new ArrayList();
	private List locals = new ArrayList();
	
	public WinDbgStackFrame(WinDbgThread thread, ICDILocation location, long pc, long frame) {
		this.thread = thread;
		this.location = location;
		this.pc = pc;
		this.frame = frame;
	}
	
	// Native Interface to populate variables
	public static native void initNative();
	static {
		initNative();
	}
	
	public native void npopulateVariables(long handle, long pc, long frame);
	
	public void populateVariables() {
		// TODO currently throwing an exception
		npopulateVariables(thread.getProcessHandle(), pc, frame);
		populated = true;
	}

	private void addVariable(boolean isArg, String name, int value) {
		WinDbgValue valueObj = new WinDbgValue(Integer.toString(value));
		if (isArg) {
			arguments.add(new WinDbgArgument(this, name, valueObj));
		} else {
			locals.add(new WinDbgVariable(this, name, valueObj));
		}
	}
	
	public ICDILocation getLocation() {
		return location;
	}

	public ICDIVariable[] getLocalVariables() throws CDIException {
		if (!populated)
			populateVariables();

		return (ICDIVariable[])locals.toArray(new ICDIVariable[0]);
	}

	public ICDIArgument[] getArguments() throws CDIException {
		if (!populated)
			populateVariables();
	
		return (ICDIArgument[])arguments.toArray(new ICDIArgument[0]);
	}

	public ICDIThread getThread() {
		return thread;
	}

	void setLevel(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}

	public boolean equals(ICDIStackFrame stackframe) {
		// TODO Auto-generated method stub
		return false;
	}

	public ICDITarget getTarget() {
		return thread.getTarget();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStepReturn#stepReturn()
	 */
	public void stepReturn() throws CDIException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStepReturn#stepReturn(org.eclipse.cdt.debug.core.cdi.model.ICDIValue)
	 */
	public void stepReturn(ICDIValue value) throws CDIException {
		// TODO Auto-generated method stub
		
	}
}