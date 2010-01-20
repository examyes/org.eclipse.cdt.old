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

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IResource;

/**
 * @author Doug Schaefer
 * 
 * A configuration is a combination of tools, a builder and one or more build targets that are executed
 * when Eclipse calls for a build.
 * 
 */
public class Configuration {

	/**
	 * @return the builder for this configuration.
	 */
	public Builder getBuilder() {
		return null;
	}

	/**
	 * @return the tool instances used to build this configuration.
	 */
	public ToolInstance[] getTools() {
		return null;
	}
	
	/**
	 * @return the target platform that executes the results of the build of this configuration.
	 */
	public TargetPlatform getTargetPlatform() {
		return null;
	}
	
	/**
	 * @param resource resource in the project for which this is a configuration
	 * @return the scanner info for the resource
	 * 
	 * Called when this configuration is the active indexer configuration for the project.
	 */
	public IScannerInfo getScannerInformation(IResource resource) {
		return null;
	}
	
}
