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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * @author Doug Schaefer
 *
 */
public class NewGCCWizardPage extends WizardPage {

	Text projectName;
	IWorkspaceRoot root;

	protected NewGCCWizardPage() {
		super("New GCC Project");
		setTitle("New GCC Project");
		setDescription("New GCC Project");
		
		root = ResourcesPlugin.getWorkspace().getRoot();
	}

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		addProjectNameSelector(comp);

		setControl(comp);
	}

	public void addProjectNameSelector(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText("Project Name");
		
		projectName = new Text(group, SWT.BORDER);
		projectName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		projectName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateProjectName();
			}
		});
	}
	
	public void validateProjectName() {
		String name = projectName.getText();
		IProject project = root.getProject(name);
		if (project.exists())
			setErrorMessage("Project exists");
		else
			setErrorMessage(null);
	}
	
	public String getProjectName() {
		return projectName.getText();
	}
	
}
