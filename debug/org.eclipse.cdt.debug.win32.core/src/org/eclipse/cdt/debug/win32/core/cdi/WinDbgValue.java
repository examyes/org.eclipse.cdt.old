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
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

public class WinDbgValue implements ICDIValue {

	private String valueString;
	
	public WinDbgValue(String value) {
		valueString = value;
	}
	
	public String getTypeName() throws CDIException {
		return "int";
	}

	public String getValueString() throws CDIException {
		return valueString;
	}

	public int getChildrenNumber() throws CDIException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean hasChildren() throws CDIException {
		// TODO Auto-generated method stub
		return false;
	}

	public ICDIVariable[] getVariables() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
	 */
	public ICDITarget getTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getType()
	 */
	public ICDIType getType() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

}
