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

import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIRuntimeOptions implements ICDIRuntimeOptions {
	
	private final WinCDITarget target;
	
	public WinCDIRuntimeOptions(WinCDITarget target) {
		this.target = target;
	}

	public void setArguments(String[] args) throws CDIException {
		// TODO Auto-generated method stub
	}

	public void setEnvironment(Properties props) throws CDIException {
		// TODO Auto-generated method stub
	}

	public void setWorkingDirectory(String wd) throws CDIException {
		// TODO Auto-generated method stub
	}

	public ICDITarget getTarget() {
		return target;
	}

}
