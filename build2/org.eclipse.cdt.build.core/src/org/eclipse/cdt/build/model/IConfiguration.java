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

import java.util.Map;

import org.eclipse.core.runtime.CoreException;

/**
 * Configuration allows for multiple sets of data to drive the build.
 * 
 * @author Doug Schaefer
 */
public interface IConfiguration {

	/**
	 * Id for this configuration.
	 * 
	 * @return id
	 */
	String getId();
	
	/**
	 * Set the user visible name for the configuration.
	 * The default name is the id.
	 * 
	 * @param name
	 */
	void setName(String name);
	
	/**
	 * User visible name for the configuration.
	 * 
	 * @return name
	 */
	String getName();
	
	/**
	 * Record the ids of the toolchains used in this configuration.
	 * 
	 * @param ids
	 */
	void setToolChainIds(String[] ids);
	
	/**
	 * Return the ids of the toolchains used in this configuration.
	 * @return
	 */
	String[] getToolChainIds();
	
	/**
	 * Sets the external build command that is executed at build time.
	 * 
	 * @param buildCommand
	 */
	void setBuildCommand(String buildCommand);
	
	/**
	 * Returns the external build command.
	 * 
	 * @return external build command
	 */
	String getBuildCommand();

	/**
	 * Sets the external command to perform the clean operation.
	 * 
	 * @param cleanCommand
	 */
	void setCleanCommand(String cleanCommand);
	
	/**
	 * Returns the clean command.
	 * 
	 * @return clean command
	 */
	String getCleanCommand();
	
	/**
	 * Sets the directory where to run the build command.
	 * 
	 * @param buildDirectory
	 */
	void setBuildDirectory(String buildDirectory);
	
	/**
	 * Returns the build directory.
	 * 
	 * @return build directory
	 */
	String getBuildDirectory();

	/**
	 * Adds an environment setting. This is passed to the environment when
	 * runnign the external commands.
	 * 
	 * @param environmentSetting
	 */
	void addEnvironmentSetting(EnvironmentSetting environmentSetting);
	
	/**
	 * Get the environment to be used for this build.
	 * 
	 * @return environment
	 */
	Map<String, String> getEnvironment() throws CoreException;

	/**
	 * Returns the ids of the error parsers to use with this configuration
	 * 
	 * @return error parser ids
	 */
	String[] getErrorParserIds() throws CoreException;
	
	/**
	 * Save changes to this configuration.
	 * 
	 * @throws CoreException
	 */
	void flush() throws CoreException;
	
}
