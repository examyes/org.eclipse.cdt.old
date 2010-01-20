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
package org.eclipse.cdt.internal.build.core.model;

import org.eclipse.cdt.build.core.ProjectBuilder;
import org.eclipse.cdt.build.core.model.IBuildProject;
import org.eclipse.cdt.build.core.model.IBuildService;
import org.eclipse.cdt.build.core.model.ITargetPlatform;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.internal.build.core.Activator;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * @author Doug Schaefer
 *
 */
public class BuildService implements IBuildService {

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unsubscribe(IResource resource,	IScannerInfoChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initBuildSpec(IProjectDescription desc) {
		ICommand[] commands = new ICommand[1];
		commands[0] = ProjectBuilder.createCommand(desc);
		desc.setBuildSpec(commands);
	}
	
	@Override
	public IBuildProject getBuildProject(IProject project) {
		return (IBuildProject)project.getAdapter(IBuildProject.class);
	}

	@Override
	public ITargetPlatform[] getTargetPlatforms() {
		IExtensionRegistry extReg = Platform.getExtensionRegistry();
		IExtensionPoint extPoint = extReg.getExtensionPoint(Activator.PLUGIN_ID, "buildDefinitions");
		IExtension[] extensions = extPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elems = extension.getConfigurationElements();
			for (IConfigurationElement elem : elems) {

			}
		}
		return new ITargetPlatform[0];
	}
	
}
