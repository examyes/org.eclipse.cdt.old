/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (WRS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.core.builders;

import java.util.Map;

import org.eclipse.cdt.build.core.model.Builder;
import org.eclipse.cdt.internal.build.core.CommandLauncher;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This builder calls an external command to do the build.
 */
public class ExternalBuilder extends Builder {

	@Override
	public IProject[] build(IncrementalProjectBuilder projectBuilder,
			int kind, Map args, IProgressMonitor monitor) throws CoreException {
		
		new CommandLauncher().exec(new String[] { "make" }, null, null, monitor);
		
		return null;
	}

	@Override
	public void clean(IProgressMonitor monitor) throws CoreException {

	}

}
