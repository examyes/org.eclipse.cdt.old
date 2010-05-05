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

package org.eclipse.cdt.internal.build.model;

import org.eclipse.cdt.build.model.IConfiguration;
import org.eclipse.cdt.build.model.IProjectBuild;
import org.eclipse.cdt.internal.build.builder.ProjectBuilder;
import org.eclipse.cdt.internal.build.core.Activator;
import org.eclipse.cdt.internal.build.core.BuildProjectNature;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * The project build object, the root of the build model. Settings are stored in
 * project preferences for the project.
 * 
 * @author Doug Schaefer
 */
public class ProjectBuild implements IProjectBuild {

	private static final String configurationsId = "configurations";
	private static final QualifiedName activeConfigId = new QualifiedName(Activator.getId(), "activeConfig");
	
	private final IProject project;
	
	public static IProjectBuild registerProject(IProject project, IProgressMonitor monitor) throws CoreException {
		// Set up the nature and builder
		IProjectDescription desc = project.getDescription();
		String[] oldNatures = desc.getNatureIds();
		String[] newNatures = new String[oldNatures.length + 1];
		System.arraycopy(oldNatures, 0, newNatures, 0, oldNatures.length);
		newNatures[oldNatures.length] = BuildProjectNature.ID;
		desc.setNatureIds(newNatures);
		
		ICommand newCommand = desc.newCommand();
		newCommand.setBuilderName(ProjectBuilder.ID);
		newCommand.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, false);
		newCommand.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, true);
		newCommand.setBuilding(IncrementalProjectBuilder.FULL_BUILD, true);
		newCommand.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, true);
		
		ICommand[] oldCommands = desc.getBuildSpec();
		ICommand[] newCommands = new ICommand[oldCommands.length + 1];
		System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);
		newCommands[oldCommands.length] = newCommand;
		desc.setBuildSpec(newCommands);
		
		project.setDescription(desc, monitor);
		
		return new ProjectBuild(project);
	}
	
	public ProjectBuild(IProject project) {
		this.project = project;
	}
	
	// Use getPreferences to get this
	private Preferences _preferences;
	
	private synchronized Preferences getPreferences() {
		if (_preferences == null)
			_preferences = new ProjectScope(project).getNode(Activator.getId());
		return _preferences;
	}
	
	public IConfiguration createConfiguration(String id) throws CoreException {
		Preferences prefs = getPreferences();
		String ids = prefs.get(configurationsId, null);
		if (ids != null)
			ids += "," + id;
		else
			ids = id;
		prefs.put(configurationsId, ids);

		return new Configuration(id, project);
	}

	private String[] getConfigIds() {
		String ids = getPreferences().get(configurationsId, null);
		return ids != null ? ids.split(",") : new String[0];
	}

	public IConfiguration getConfiguration(String id) {
		String[] ids = getConfigIds();
		for (String testid : ids)
			if (testid.equals(ids))
				return new Configuration(id, project);
		
		return null;
	}

	public IConfiguration[] getConfigurations() {
		String[] ids = getConfigIds();
		IConfiguration[] configs = new IConfiguration[ids.length];
		for (int i = 0; i < ids.length; ++i)
			configs[i] = new Configuration(ids[i], project);
			
		return configs;
	}

	public void setActiveConfiguration(IConfiguration configuration) throws CoreException {
		project.setPersistentProperty(activeConfigId, configuration.getId());
	}

	public IConfiguration getActiveConfiguration() throws CoreException {
		String activeConfig = project.getPersistentProperty(activeConfigId);
		if (activeConfig != null) {
			IConfiguration config = getConfiguration(activeConfig);
			if (config != null)
				return config;
		}
		
		String ids = getPreferences().get(configurationsId, null);
		if (ids == null)
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "no configurations on " + project.getName()));
		int i = ids.indexOf(',');
		if (i < 0)
			return new Configuration(ids, project);
		else
			return new Configuration(ids.substring(0, i), project);
	}

	public synchronized void flush() throws CoreException {
		try {
			if (_preferences != null) {
				_preferences.flush();
				_preferences = null;
			}
		} catch (BackingStoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), e.getLocalizedMessage(), e));
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		flush();
		super.finalize();
	}
	
}
