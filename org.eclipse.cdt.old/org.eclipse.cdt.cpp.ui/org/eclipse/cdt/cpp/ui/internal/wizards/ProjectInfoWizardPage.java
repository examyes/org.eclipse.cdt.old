package com.ibm.cpp.ui.internal.wizards;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;

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
    
    /**
     * Creates a new class page.
     */
    public ProjectInfoWizardPage() 
    {
	super("ProjectInfoWizardPage");
	setTitle(_plugin.getLocalizedString("createProjectWizard.Info.Title"));
	setDescription(_plugin.getLocalizedString("createProjectWizard.Info.Description"));
	//	setImageDescriptor(titleImage);
	setPageComplete(false);
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

    protected void enter(int direction) 
    {
	setPageComplete(true);
    }

    String getClassName() 
    {
	return text.getText();
    }
}
