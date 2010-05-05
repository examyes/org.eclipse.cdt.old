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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.build.model.EnvironmentSetting;
import org.eclipse.cdt.build.model.IBuildService;
import org.eclipse.cdt.build.model.IConfiguration;
import org.eclipse.cdt.build.model.IToolChain;
import org.eclipse.cdt.internal.build.core.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author Doug Schaefer
 */
public class Configuration implements IConfiguration {

	// ids for preferences
	private static final String NAME = "name";
	private static final String TOOLCHAINS = "toolchains";
	private static final String BUILD_COMMAND = "buildCommand";
	private static final String CLEAN_COMMAND = "cleanCommand";
	private static final String BUILD_DIRECTORY = "buildDirectory";
	
	// default values
	private static final String DEFAULT_BUILD_COMMAND = "make";
	private static final String DEFAULT_CLEAN_COMMAND = "make clean";
	
	private final String id;
	private final IProject project;
	
	public Configuration(String id, IProject project) {
		this.id = id;
		this.project = project;
	}
	
	public String getId() {
		return id;
	}

	// Use getPreferences to get this
	private Preferences _preferences;
	
	private synchronized Preferences getPreferences() {
		if (_preferences == null)
			_preferences = new ProjectScope(project).getNode(Activator.getId()).node("configuration." + id);
		return _preferences;
	}
	
	public void setName(String name) {
		Preferences prefs = getPreferences();
		prefs.put(NAME, name);
	}

	public String getName() {
		String name = getPreferences().get(NAME, null);
		if (name == null)
			name = id;
		return name;
	}

	public void setToolChainIds(String[] ids) {
		Preferences prefs = getPreferences();
		if (ids == null || ids.length == 0)
			prefs.remove(TOOLCHAINS);
		else {
			StringBuffer idBuff = new StringBuffer();
			idBuff.append(ids[0]);
			for (int i = 1; i < ids.length; ++i) {
				idBuff.append(',');
				idBuff.append(ids[i]);
			}
			prefs.put(TOOLCHAINS, idBuff.toString());
		}
	}
	
	public String[] getToolChainIds() {
		String idString = getPreferences().get(TOOLCHAINS, null);
		if (idString == null)
			return new String[0];
		else
			return idString.split(",");
	}
	
	public void setBuildCommand(String buildCommand) {
		Preferences prefs = getPreferences();
		if (DEFAULT_BUILD_COMMAND.equals(buildCommand))
			prefs.remove(BUILD_COMMAND);
		else
			prefs.put(BUILD_COMMAND, buildCommand);
	}

	public String getBuildCommand() {
		String cmd = getPreferences().get(BUILD_COMMAND, null);
		if (cmd == null)
			cmd = DEFAULT_BUILD_COMMAND;
		return cmd;
	}

	public void setCleanCommand(String cleanCommand) {
		Preferences prefs = getPreferences();
		if (DEFAULT_CLEAN_COMMAND.equals(cleanCommand))
			prefs.remove(CLEAN_COMMAND);
		else
			prefs.put(CLEAN_COMMAND, cleanCommand);
	}

	public String getCleanCommand() {
		String cmd = getPreferences().get(CLEAN_COMMAND, null);
		if (cmd == null)
			cmd = DEFAULT_CLEAN_COMMAND;
		return cmd;
	}

	public void setBuildDirectory(String buildDirectory) {
		Preferences prefs = getPreferences();
		if (new Path(buildDirectory).equals(project.getLocation()))
			prefs.remove(BUILD_DIRECTORY);
		else
			prefs.put(BUILD_DIRECTORY, buildDirectory);
	}

	public String getBuildDirectory() {
		String dir = getPreferences().get(BUILD_DIRECTORY, null);
		if (dir == null) {
			IPath path = project.getLocation();
			if (path != null)
				dir = path.toOSString();
		}
		return dir;
	}

	public void addEnvironmentSetting(EnvironmentSetting environmentSetting) {
		// TODO Auto-generated method stub

	}

	public Map<String, String> getEnvironment() throws CoreException {
		Map<String, String> env = new HashMap<String, String>();
		env.putAll(System.getenv());
		
		IToolChain[] toolChains = Activator.getService(IBuildService.class).getToolChains(getToolChainIds());
		// Go backwards through the toolchains. The first one wins.
		for (int i = toolChains.length - 1; i >= 0; --i) {
			EnvironmentSetting[] settings = toolChains[i].getEnvironmentSettings();
			for (EnvironmentSetting setting : settings) {
				String value;
				switch (setting.getOperation()) {
				case replace:
					env.put(setting.getVariable(), setting.getValue());
					break;
				case prepend:
					value = env.get(setting.getVariable());
					if (value != null)
						value = setting.getValue() + value;
					else
						value = setting.getValue();
					env.put(setting.getVariable(), value);
					break;
				case append:
					value = env.get(setting.getVariable());
					if (value != null)
						value += setting.getValue();
					else
						value = setting.getValue();
					env.put(setting.getVariable(), value);
					break;
				}
			}
		}
		
		// TODO Override with config env
		
		return env;
	}
	
	public synchronized void flush() throws CoreException {
		try {
			if (_preferences != null)
				_preferences.flush();
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
