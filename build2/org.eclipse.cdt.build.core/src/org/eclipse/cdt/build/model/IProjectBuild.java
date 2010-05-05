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

package org.eclipse.cdt.build.model;

import org.eclipse.core.runtime.CoreException;

/**
 * The build object for a given project.
 * 
 * @author Doug Schaefer
 */
public interface IProjectBuild {
	
	/**
	 * Create a configuration on the project with the given id.
	 * 
	 * @param id
	 * @return new configuration
	 */
	IConfiguration createConfiguration(String id) throws CoreException;

	/**
	 * Return the configuration with the given id.
	 * 
	 * @param id
	 * @return configuration
	 */
	IConfiguration getConfiguration(String id);
	
	/**
	 * Return all configurations for this project.
	 * 
	 * @return all configurations
	 */
	IConfiguration[] getConfigurations();

	/**
	 * Sets the active configuration for the project. This is the
	 * configuration that controls the Eclipse build. It is a local setting
	 * that is stored in the users preference data.
	 * 
	 * @param configuration
	 */
	void setActiveConfiguration(IConfiguration configuration) throws CoreException;
	
	/**
	 * Returns the active configuration.
	 * 
	 * @return active configuration
	 */
	IConfiguration getActiveConfiguration() throws CoreException;
	
	/**
	 * Save recent changes to the build object.
	 * 
	 * @throws CoreException
	 */
	void flush() throws CoreException;
}
