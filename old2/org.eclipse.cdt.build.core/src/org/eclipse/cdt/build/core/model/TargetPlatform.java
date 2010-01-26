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
import org.eclipse.core.runtime.IExtension;

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
public abstract class TargetPlatform {
	
	private String id;
	private String name;
	
	/**
	 * Load the target platform from the extension.
	 *
	 * @param extension
	 */
	public void load(IExtension extension) {
		id = extension.getUniqueIdentifier();
		name = extension.getLabel();
	}
	
	/**
	 * @return the name of this platform.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Give the environment we're running in right now, will this target platform produce
	 * builds successfully. This let's us hide unavailable builds in the new project wizard
	 * and give the user warnings when attempting to use configurations for this platform.
	 * 
	 * @return is this target platform available for builds
	 */
	public abstract boolean isAvailable();
	
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
