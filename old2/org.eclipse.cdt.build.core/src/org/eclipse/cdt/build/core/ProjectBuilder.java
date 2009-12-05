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

import java.util.Map;

import org.eclipse.cdt.internal.build.core.Activator;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The Project Builder for the CDT.
 * 
 * @author Doug Schaefer
 *
 */
public class ProjectBuilder extends IncrementalProjectBuilder {

	// Extension ID
	private static String ID = Activator.PLUGIN_ID + ".builder";
	
	public static ICommand createCommand(IProjectDescription description) {
		ICommand command = description.newCommand();
		command.setBuilderName(ID);
		command.setBuilding(FULL_BUILD, true);
		command.setBuilding(INCREMENTAL_BUILD, true);
		command.setBuilding(CLEAN_BUILD, true);
		// Auto builds don't make much sense with external tools that take a long time to run
		command.setBuilding(AUTO_BUILD, false);
		return command;
	}
	
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
	}
	
}
