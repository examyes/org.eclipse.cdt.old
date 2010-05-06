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

import org.eclipse.cdt.build.model.DiscoveredCommand;
import org.eclipse.cdt.build.model.IBuildService;
import org.eclipse.cdt.build.model.IProjectBuild;
import org.eclipse.cdt.build.model.IToolChain;
import org.eclipse.cdt.internal.build.core.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Build Service implementation.
 * 
 * @author Doug Schaefer
 */
public class BuildService implements IBuildService {

	public IProjectBuild registerProject(IProject project, IProgressMonitor monitor) throws CoreException{
		return ProjectBuild.registerProject(project, monitor);
	}

	public IProjectBuild getProjectBuild(IProject project) {
		// TODO test to make sure our nature is present
		return new ProjectBuild(project);
	}

	public IToolChain getToolChain(String id) throws CoreException {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.getId(), "toolChain");
		if (point != null) {
			IExtension extension = point.getExtension(id);
			if (extension != null) {
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().equals("implementation")) {
						return (IToolChain)element.createExecutableExtension("class");
					}
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "toolchain " + id + " is not installed"));
	}

	public IToolChain[] getToolChains(String[] ids) throws CoreException {
		IToolChain[] toolchains = new IToolChain[ids.length];
		for (int i = 0; i < ids.length; ++i)
			toolchains[i] = getToolChain(ids[i]);
		return toolchains;
	}
	
	public String[] getToolChainIds() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getToolChainName(String id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public DiscoveredCommand getDiscoveredCommand(IResource resource) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
