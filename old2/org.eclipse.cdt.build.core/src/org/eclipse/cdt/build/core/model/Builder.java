/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (WRS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.core.model;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Each configuration has a builder that does the actual build. It implements the technique of the
 * based on settings in the configuration.
 */
public abstract class Builder {

	private Configuration configuration;
	
	/**
	 * This will mainly be used internally by this builder to access the tools and options.
	 * 
	 * @return the configuration that this builder builds.
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Set the configuration to be used by this builder.
	 * 
	 * @param configuration
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * The build method implementation for the IncrementalProjectBuilder.
	 *
	 * @param projectBuilder
	 * @param kind
	 * @param args
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public abstract IProject[] build(IncrementalProjectBuilder projectBuilder,
			int kind, Map args, IProgressMonitor monitor) throws CoreException;

	/**
	 * The clean method implementation for the IncrementalProjectBuilder.
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	public abstract void clean(IProgressMonitor monitor) throws CoreException;

}
