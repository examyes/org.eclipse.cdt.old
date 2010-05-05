/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.build.ui;

import org.eclipse.cdt.build.model.EnvironmentSetting;
import org.eclipse.cdt.build.model.IToolChain;

/**
 * The toolchain definition for gcc.
 * Note this is here mainly for testing. It should migrate to a
 * real gcc plug-in some time.
 * 
 * @author Doug Schaefer
 */
public class GCCToolChain implements IToolChain {

	public static final String ID = "org.eclipse.cdt.build.gcc.toolchain";
	public static final String NAME = "GCC";
		
	public String getId() {
		return ID; 
	}

	public String getName() {
		return NAME;
	}

	public EnvironmentSetting[] getEnvironmentSettings() {
		return new EnvironmentSetting[0];
	}

}
