/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.core.model;

import org.eclipse.cdt.core.IBinaryParser;

/**
 * @author Doug Schaefer
 * 
 * A target platform is the executable environment that the build target will execute on.
 * Examples include Windows, Linux, Cross-Linux, etc.
 * 
 * This is modeled explicitly since different target platforms will have different build targets
 * and it helps separate out the applicable toolchains which generally support only a single target
 * platform.
 */
public class TargetPlatform {
	
	/**
	 * Return a list of Builders that can build for this target platform.
	 * 
	 * @return list of builders
	 */
	public Builder[] getApplicableBuilders() {
		return null;
	}
	
	/**
	 * Return the binary parser used to parse binaries for this target platform.
	 * 
	 * @return binary parser
	 */
	public IBinaryParser getBinaryParser() {
		return null;
	}

	/**
	 * @return the minimum set of tools that are used to build for this target platform.
	 */
	public Tool[] getDefaultTools() {
		return null;
	}

}
