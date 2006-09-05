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

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @author Doug Schaefer
 *
 */
public class DependencyGenerator implements IManagedDependencyGenerator2 {

	public int getCalculatorType() {
		return IManagedDependencyGenerator2.TYPE_CUSTOM;
	}

	public String getDependencyFileExtension(IConfiguration buildContext, ITool tool) {
		return "";
	}

	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		return null;
	}

	public IManagedDependencyInfo getDependencySourceInfo(IPath source, IResource resource, IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		return new DependencyCalculator(source, resource, buildContext, tool, topBuildDirectory);
	}

	public boolean postProcessDependencyFile(IPath dependencyFile, IConfiguration buildContext, ITool tool, IPath topBuildDirectory) {
		return false;
	}

}
