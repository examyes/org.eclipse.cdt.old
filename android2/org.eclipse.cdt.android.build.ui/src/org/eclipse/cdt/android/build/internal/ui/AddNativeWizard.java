/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.android.build.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.android.build.core.NDKManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;

public class AddNativeWizard extends Wizard {

	private final IProject project;
	private final IWorkbenchWindow window;
	
	private AddNativeWizardPage addNativeWizardPage;
	private Map<String, String> templateArgs = new HashMap<String, String>();
	
	public AddNativeWizard(IProject project, IWorkbenchWindow window) {
		this.project = project;
		this.window = window;
		templateArgs.put(NDKManager.LIBRARY_NAME, project.getName());
	}
	
	@Override
	public void addPages() {
		addNativeWizardPage = new AddNativeWizardPage(templateArgs);
		addPage(addNativeWizardPage);
	}
	
	@Override
	public boolean performFinish() {
		// Switch to C/C++ Perspective
		try {
			window.getWorkbench().showPerspective(CUIPlugin.ID_CPERSPECTIVE, window);
		} catch (WorkbenchException e1) {
			Activator.log(e1);
		}
		
		addNativeWizardPage.updateArgs(templateArgs);
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException,	InterruptedException {
				IWorkspaceRunnable op = new IWorkspaceRunnable() {
					@Override
					public void run(IProgressMonitor monitor) throws CoreException {
						// Convert to CDT project
						CCorePlugin.getDefault().convertProjectToCC(project, monitor, MakeCorePlugin.MAKE_PROJECT_ID);
						// Set up build information
						new NDKWizardHandler().convertProject(project, monitor);
						// Run the template
						NDKManager.addNativeSupport(project, templateArgs, monitor);
					}
				};
				// TODO run from a job
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				try {
					workspace.run(op, workspace.getRoot(), 0, new NullProgressMonitor());
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
		try {
			getContainer().run(false, true, op);
			return true;
		} catch (InterruptedException e) {
			Activator.log(e);
			return false;
		} catch (InvocationTargetException e) {
			Activator.log(e);
			return false;
		}
	}

}
