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
package org.eclipse.cdt.build.core;

import org.eclipse.cdt.build.core.model.TargetPlatform;
import org.eclipse.core.resources.IProjectDescription;

/**
 * The OSGi Service access point into the CDT Build System.
 * 
 * This service services as a scanner info provider.
 *  
 * @author Doug Schaefer
 */
public interface IBuildService {

	/**
	 * Initialize the build spec for the project to invoke our build service
	 * @param desc
	 */
	void initBuildSpec(IProjectDescription desc);
	
	/**
	 * Get all registered target platforms.
	 * 
	 * @return target platforms
	 */
	TargetPlatform[] getTargetPlatforms();
	
}
