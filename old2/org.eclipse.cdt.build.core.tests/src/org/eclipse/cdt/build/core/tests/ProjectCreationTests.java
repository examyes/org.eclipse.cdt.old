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
package org.eclipse.cdt.build.core.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.build.core.IBuildService;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.build.core.ScannerInfoProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

/**
 * @author Doug Schaefer
 *
 */
public class ProjectCreationTests {

	private IProject createNewBuildProject(String name) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(name);

		final IProjectDescription description = workspace.newProjectDescription(name);
		String[] natures = new String[1];
		natures[0] = CProjectNature.C_NATURE_ID;
		description.setNatureIds(natures);
		
		Activator.getService(IBuildService.class).initBuildSpec(description);
		project.create(description, new NullProgressMonitor());
		project.open(new NullProgressMonitor());
		return project;
	}
	
	@Test
	public void testScannerInfo() throws Exception {
		IProject project = createNewBuildProject("scannerInfoTest");
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		assertTrue(provider instanceof ScannerInfoProvider);
		project.delete(true, new NullProgressMonitor());
	}
}
