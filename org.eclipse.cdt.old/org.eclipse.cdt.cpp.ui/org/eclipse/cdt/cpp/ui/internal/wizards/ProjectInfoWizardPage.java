package org.eclipse.cdt.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.jface.wizard.*;

public class ProjectInfoWizardPage extends WizardPage {

    public BuildInvocationWorkbookPage _workbookPageBuildInvocation;
    public EnvironmentWorkbookPage     _workbookPageEnvironment;

    private Text text;
    protected CppPlugin    _plugin = CppPlugin.getPlugin();
    private CppNewProjectResourceWizard _wizard;

    /**
     * Creates a new class page.
     */
    public ProjectInfoWizardPage(CppNewProjectResourceWizard wizard) 
    {
	super("ProjectInfoWizardPage");
	setTitle(_plugin.getLocalizedString("createProjectWizard.Info.Title"));
	setDescription(_plugin.getLocalizedString("createProjectWizard.Info.Description"));
	//	setImageDescriptor(titleImage);
	setPageComplete(false);
	_wizard = wizard;
    }
    
    /**
     * Updates the page's complete state.
     */	
    private void checkCompleteState() 
    {
	boolean b= text.getText().length() != 0;
	setPageComplete(b);
    }
    
    /**
     * Creates the page's UI content.
     */
    public void createControl(Composite parent)
    {		
           Composite composite = new Composite(parent, SWT.NONE);

           TabFolder folder = new TabFolder(composite, SWT.NONE);
           folder.setLayout(new GridLayout());
           GridData gdFolder= new GridData(GridData.FILL_HORIZONTAL);
           folder.setLayoutData(gdFolder);

          	//page 1 		 
           TabItem item1;
           _workbookPageBuildInvocation = new BuildInvocationWorkbookPage(folder);
           item1 = new TabItem(folder, SWT.NONE);
           item1.setText(_plugin.getLocalizedString("createProjectWizard.Info.BuildTab"));
           item1.setData(_workbookPageBuildInvocation);
           item1.setControl(_workbookPageBuildInvocation.getControl());

	  
	   //page 3
           TabItem item3;
           _workbookPageEnvironment = new EnvironmentWorkbookPage(folder);
           item3 = new TabItem(folder, SWT.NONE);
           item3.setText("Environment");
           item3.setData(_workbookPageEnvironment);
	   item3.setControl(_workbookPageEnvironment.getControl());

	   composite.setLayout(new FillLayout());

	   setPageComplete(true);
           setControl(composite);
	}

    public void setVisible(boolean flag)
    {
	if (flag)
	    {
		enter(1);
	    }
	super.setVisible(flag);
    }

    public void enter(int direction) 
    {
	_workbookPageEnvironment.setRemote(_wizard.isRemote());
	setPageComplete(true);
    }

    String getClassName() 
    {
	return text.getText();
    }
}
