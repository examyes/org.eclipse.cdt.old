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

package org.eclipse.cdt.build.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.build.gcc.GCCToolChain;
import org.eclipse.cdt.build.model.IBuildService;
import org.eclipse.cdt.build.model.IConfiguration;
import org.eclipse.cdt.build.model.IProjectBuild;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * @author Doug Schaefer
 *
 */
public class NewGCCWizard extends Wizard implements INewWizard {

	private IWorkbench workbench;
	private NewGCCWizardPage page;
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		setWindowTitle("New GCC Project");
	}

	@Override
	public void addPages() {
		page = new NewGCCWizardPage();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		// Switch to the C perspective
		try {
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			workbench.showPerspective("org.eclipse.cdt.ui.CPerspective", window); //$NON-NLS-1
		} catch (WorkbenchException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}

		final String projectName = page.getProjectName();
		
		IRunnableWithProgress op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {
				monitor.beginTask("Creating project", 10);
				
				// Create Project
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IProject project = workspace.getRoot().getProject(projectName);
				
				// TODO handle the case where a .project file was already there
				IProjectDescription description = workspace.newProjectDescription(project.getName());
				
				CCorePlugin.getDefault().createCDTProject(description, project, monitor);
				CCProjectNature.addCCNature(project, new SubProgressMonitor(monitor, 1));
				
				// Set up build information
				IBuildService buildService = Activator.getService(IBuildService.class);
				IProjectBuild projectBuild = buildService.registerProject(project, monitor);
				IConfiguration configuration = projectBuild.createConfiguration("default");
				configuration.setToolChainIds(new String[] { GCCToolChain.ID });
				
				configuration.flush();
				projectBuild.flush();
				
				monitor.done();
			}
		};
		
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
