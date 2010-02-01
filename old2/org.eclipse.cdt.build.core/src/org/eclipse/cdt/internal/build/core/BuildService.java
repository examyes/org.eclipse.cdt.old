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
package org.eclipse.cdt.internal.build.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.build.core.IBuildConsole;
import org.eclipse.cdt.build.core.IBuildService;
import org.eclipse.cdt.build.core.ProjectBuilder;
import org.eclipse.cdt.build.core.model.TargetPlatform;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
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
	public void initBuildSpec(IProjectDescription desc) {
		ICommand[] commands = new ICommand[1];
		commands[0] = ProjectBuilder.createCommand(desc);
		desc.setBuildSpec(commands);
	}
	
	@Override
	public TargetPlatform[] getTargetPlatforms() throws CoreException {
		List<TargetPlatform> targetPlatforms = new ArrayList<TargetPlatform>();
		IExtensionRegistry extReg = Platform.getExtensionRegistry();
		IExtensionPoint extPoint = extReg.getExtensionPoint(Activator.PLUGIN_ID, "targetPlatform"); //$NON-NLS-1$
		IExtension[] extensions = extPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			if (elements.length < 1)
				continue;
			// There is only one element in this extension point.
			TargetPlatform targetPlatform = (TargetPlatform)elements[0].createExecutableExtension("class"); //$NON-NLS-1$
			targetPlatform.load(extension);
			targetPlatforms.add(targetPlatform);
		}
		return targetPlatforms.toArray(new TargetPlatform[targetPlatforms.size()]);
	}
	
	@Override
	public IBuildConsole getBuildConsole() throws CoreException {
		IExtensionRegistry extReg = Platform.getExtensionRegistry();
		IExtensionPoint extPoint = extReg.getExtensionPoint(Activator.PLUGIN_ID, "buildConsole"); //$NON-NLS-1$
		IExtension[] extensions = extPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			if (elements.length < 1)
				continue;
			IBuildConsole buildConsole = (IBuildConsole)elements[0].createExecutableExtension("class");
			if (buildConsole.isAvailable())
				return buildConsole;
		}
		// Non available, use the standard one
		return new StdBuildConsole();
	}
	
}
