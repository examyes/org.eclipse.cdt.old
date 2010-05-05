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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Root service for the build system.
 * 
 * @author Doug Schaefer
 */
public interface IBuildService {

	/**
	 * Register the project as a client for the build system.
	 *
	 * @param project
	 * @return
	 */
	IProjectBuild registerProject(IProject project, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Return a build object for the given project.
	 * 
	 * @param project
	 * @return build object
	 */
	IProjectBuild getProjectBuild(IProject project);

	/**
	 * Return the given toolchain.
	 * 
	 * @param id
	 * @return toolchain
	 */
	IToolChain getToolChain(String id) throws CoreException;
	
	/**
	 * Return the toolchains for the list of ids.
	 * 
	 * @param ids
	 * @return toolchains
	 * @throws CoreException
	 */
	IToolChain[] getToolChains(String[] ids) throws CoreException;
	
	/**
	 * Return all registered toolchain ids.
	 * 
	 * @return toolchain ids
	 */
	String[] getToolChainIds();
	
	/**
	 * Return the User displayable name for the toolchain.
	 * This is used to avoid loading all toolchains at project creation time.
	 * 
	 * @param id
	 * @return
	 */
	String getToolChainName(String id);
	
}
