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

import org.eclipse.core.resources.IProject;

/**
 * This is an extension of IProject to add build information.
 * You can get an the associated instance of this object for a given project by
 * adapting the project to IBuildProject.
 * 
 * @author Doug Schaefer
 *
 */
public class BuildProject {

	private final IProject project;
	
	private BuildProject(IProject project) {
		this.project = project;
	}
	
	/**
	 * @param project
	 * @return the build object for the project
	 */
	public static BuildProject getBuildProject(IProject project) {
		// TODO cache these things
		return new BuildProject(project);
	}
	
	/**
	 * @return the project this object is associated with.
	 */
	public IProject getProject() {
		return project;
	}
	
	/**
	 * @return the configurations for this project.
	 */
	public Configuration[] getConfigurations() {
		return null;
	}

	/**
	 * @return the current active configuration for system builds.
	 */
	public Configuration getActiveBuildConfiguration() {
		// TODO just testing
		return new Configuration();
	}
	
	/**
	 * @return the current active configuration for indexing.
	 */
	public Configuration getActiveIndexConfiguration() {
		return null;
	}
	
}
