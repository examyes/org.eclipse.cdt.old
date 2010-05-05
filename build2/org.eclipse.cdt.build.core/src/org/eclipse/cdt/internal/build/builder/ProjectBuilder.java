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

package org.eclipse.cdt.internal.build.builder;

import java.io.IOException;
import java.util.Map;

import org.eclipse.cdt.build.model.IBuildService;
import org.eclipse.cdt.build.model.IConfiguration;
import org.eclipse.cdt.build.model.IProjectBuild;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.build.core.Activator;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class ProjectBuilder extends ACBuilder {

	public static final String ID = "org.eclipse.cdt.build.builder";
	
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		
		// Get the active configuration
		IBuildService buildService = Activator.getService(IBuildService.class);
		IProjectBuild projectBuild = buildService.getProjectBuild(project);
		IConfiguration config = projectBuild.getActiveConfiguration();
		
		// The build command
		String commandStr = kind == CLEAN_BUILD
			? config.getCleanCommand()
			: config.getBuildCommand();
		String[] command = commandStr.split("\\s+");

		if (command.length == 0)
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "no build command"));
		
		IPath commandPath = new Path(command[0]);
		
		String[] commandArgs;
		if (command.length > 1) { 
			commandArgs = new String[command.length - 1];
			System.arraycopy(command, 1, commandArgs, 0, commandArgs.length);
		} else
			commandArgs = new String[0];

		// The environment
		Map<String, String> envMap = config.getEnvironment();
		String[] env = new String[envMap.size()];
		int i = 0;
		for (String key : envMap.keySet())
			env[i++] = key + "=" + envMap.get(key);
		
		IPath dir = new Path(config.getBuildDirectory());
		
		// The build console
		IConsole console = CCorePlugin.getDefault().getConsole();
		console.start(project);
		
		// remove all markers
		IMarker[] markers = project.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		if (markers != null) {
			project.getWorkspace().deleteMarkers(markers);
		}

		// The error parser
		String[] errorParserIds = config.getErrorParserIds();
		ErrorParserManager errorParser = new ErrorParserManager(project, dir.toFile().toURI(),
				this, errorParserIds);
		errorParser.setOutputStream(console.getOutputStream());

		// Launch the build
		ICommandLauncher launcher = new CommandLauncher();
		Process p = launcher.execute(commandPath, commandArgs, env, dir, monitor);
		try {
			// Close outgoing pipe to process
			p.getOutputStream().close();
		} catch (IOException e) {
			Activator.getService(ILog.class).log(new Status(IStatus.WARNING, Activator.getId(), e.getLocalizedMessage(), e));
		}
		
		// Run and process output
		launcher.waitAndRead(errorParser, errorParser, monitor);
		
		// Refresh to pick up the build results
		project.refreshLocal(IProject.DEPTH_INFINITE, monitor);
		
		// TODO should return the referenced projects
		// so that we build when they change
		return new IProject[0];
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		build(CLEAN_BUILD, null, monitor);
	}
	
}
