/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.windows.build;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;

/**
 * @author Doug Schaefer
 *
 */
public class NMakefileGenerator implements IManagedBuilderMakefileGenerator {

	private IPath buildWorkingDir;
	
	private static final String makefileName = "Makefile";
	
	public void generateDependencies() throws CoreException {
	}

	public MultiStatus generateMakefiles(IResourceDelta delta) throws CoreException {
		// for now regenerate the makefiles every time
		// later we'll check so that we only generate them if we need to
		return regenerateMakefiles();
	}

	public IPath getBuildWorkingDir() {
		return buildWorkingDir;
	}

	public String getMakefileName() {
		return makefileName;
	}

	public void initialize(IProject project, IManagedBuildInfo info, IProgressMonitor monitor) {
		IConfiguration config = info.getDefaultConfiguration();
		buildWorkingDir = new Path(config.getName());
	}

	public boolean isGeneratedResource(IResource resource) {
		return buildWorkingDir.isPrefixOf(resource.getFullPath());
	}

	public void regenerateDependencies(boolean force) throws CoreException {
		// dependencies are in the makefile
		// which gets generated every time for now
		// so do nothing
	}

	public MultiStatus regenerateMakefiles() throws CoreException {
		return new MultiStatus(
				ManagedBuilderCorePlugin.getUniqueIdentifier(),
				IStatus.OK,
				new String(),
				null);
	}

}
