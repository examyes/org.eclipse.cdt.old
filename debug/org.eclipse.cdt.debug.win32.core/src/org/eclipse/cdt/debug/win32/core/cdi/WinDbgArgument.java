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
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

public class WinDbgArgument implements ICDIArgument {

	private ICDIStackFrame frame;
	private String name;
	private ICDIValue value;
	
	public WinDbgArgument(ICDIStackFrame frame, String name, ICDIValue value) {
		this.frame = frame;
		this.name = name;
		this.value = value;
	}

	public ICDIValue getValue() throws CDIException {
		return value;
	}

	public void setValue(String expression) throws CDIException {
		value = new WinDbgValue(expression);
	}

	public void setValue(ICDIValue value) throws CDIException {
		this.value = value;
	}

	public void setFormat(int format) throws CDIException {
		// TODO Auto-generated method stub
	}

	public String getName() {
		return name;
	}

	public ICDIStackFrame getStackFrame() throws CDIException {
		return frame;
	}

	public ICDIType getType() throws CDIException {
		// TODO 
		return null;
	}

	public String getTypeName() throws CDIException {
		// TODO Auto-generated method stub
		return value.getTypeName();
	}

	public int sizeof() throws CDIException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isEditable() throws CDIException {
		// TODO Auto-generated method stub
		return false;
	}

	public String getQualifiedName() throws CDIException {
		// TODO Auto-generated method stub
		return name;
	}

	public boolean equals(ICDIVariableObject varObject) {
		// TODO Auto-generated method stub
		return false;
	}

	public ICDITarget getTarget() {
		return frame.getTarget();
	}

}
