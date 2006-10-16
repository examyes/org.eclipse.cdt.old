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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCalculator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Doug Schaefer
 */
public class DependencyCalculator implements IManagedDependencyCalculator {
	
	private final IPath source;
	private final IResource resource;
	private final IBuildObject buildContext;
	private final ITool tool;
	private final IPath topBuildDirectory;
	private IPath[] dependencies;
	
	public DependencyCalculator(IPath source, IResource resource, IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		this.source = source;
		this.resource = resource;
		this.buildContext = buildContext;
		this.tool = tool;
		this.topBuildDirectory = topBuildDirectory;
	}
	
	public IPath[] getAdditionalTargets() {
		return null;
	}

	public IPath[] getDependencies() {
		return new IPath[0];
	}

	public IBuildObject getBuildContext() {
		return buildContext;
	}

	public IPath getSource() {
		return source;
	}

	public ITool getTool() {
		return tool;
	}

	public IPath getTopBuildDirectory() {
		return topBuildDirectory;
	}

}
