package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.vcm.*;

import org.eclipse.core.resources.*;
import org.eclipse.jface.window.*;
import org.eclipse.core.runtime.*;
import java.util.*;
import org.eclipse.core.resources.IFolder;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.navigator.*;
import org.eclipse.jface.wizard.Wizard;


import com.ibm.dstore.core.model.*;

/**
 *  This is target creation wizard
 */
public class NewTargetWizard extends org.eclipse.jface.wizard.Wizard implements INewWizard 
{
    private IWorkbench workbench;
    private IStructuredSelection selection;
    private NewWizardTargetMainPage mainPage;
    private int index = -1;
    
    private DataElement _resource;
    private CppPlugin  _pluginInstance = CppPlugin.getPlugin();
    

    // NL enablement
    private String Wizard_TITLE = "TargetsViewer.Wizard.Title";
    
    /**
     *	Answer self's Wizard, optionally utilizing the passed workbench
     *	and current resource selection.  Since in this case self is the
     *	Wizard, simply answer self
     *
     *	@return	Wizard
     *	@param	IWorkbench
     *	@param	selection IStructuredSelection
     */
    public void init(IWorkbench aWorkbench,IStructuredSelection currentSelection) 
    {	
	workbench = aWorkbench;
	if(currentSelection!=null && currentSelection instanceof IStructuredSelection)
	{
	    Object root = ((IStructuredSelection)currentSelection).getFirstElement();
	    if (root instanceof IResource)
		{	
		    IProject project = null; 
		    if(root instanceof IProject)
			{
			    project = (IProject)root;
			}
		    else if (root instanceof IFolder)
			{
			    project = ((IFolder)root).getProject();
			}
		    else if (root instanceof IFile)
			{
			    project = ((IFile)root).getProject();
			}
		    
		    
		}
	}

	selection = currentSelection; 
	setDefaultPageImageDescriptor(CppPluginImages.DESC_WIZBAN_NEWTARGET_WIZ);
    }


    /**
     *	Create the wizard pages
     */
    public void addPages() 
    {
	super.addPages();
	mainPage = new NewWizardTargetMainPage(_pluginInstance.getLocalizedString(Wizard_TITLE),selection);
	addPage(mainPage);
    }


    /**
     *	Completes processing of the wizard. If this method returns true,
     *	the wizard will close; otherwise, it will stay active.
     */
    public boolean performFinish() 
    {    
	final Targets targetsPart;
	final TargetElement target = mainPage.getNewTarget();
	DataElement projectElement = mainPage.getProjectElement(); 
	ModelInterface api = ModelInterface.getInstance();
	final IProject root = api.findProjectResource(projectElement);

	if (target != null) 
	    {
		// here put the code to open up the make targets view and show the new created target	
		try 
		    {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("com.ibm.cpp.ui.Targets");
		    } 
		catch (PartInitException e) 
		    {
			System.out.println(""+e);
		    }

		// add the new target to the page
		targetsPart = (Targets)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("com.ibm.cpp.ui.Targets");
		final TargetsPage targetsPage = targetsPart.getTargetsPage();


		final TargetsViewer viewer = targetsPage.getViewer();
		if(root!=null)
		    {
			index = targetsPage.getRootIndex(root);
			if(index < 0)
			{
				System.out.println("\n root has been added");
				targetsPage.targetStore.projectList.add(new RootElement(root, null));
			}

			viewer.getControl().getDisplay().asyncExec(new Runnable() 
			    {
				public void run() 
				{
				    int index = targetsPage.getRootIndex(root);// get root element index
				    // must call this function inorder to create new descriptors for the properties for the new targets
				    RootElement rootElement =  (RootElement)targetsPage.targetStore.projectList.elementAt(index);
				    rootElement.setProprtyDescriptor();
				    target.setParent(rootElement);
				    rootElement.add(target);
				    java.util.List list = new ArrayList();
				    list.add((RootElement)targetsPage.targetStore.projectList.elementAt(index));
				    viewer.setInput(list.toArray());
			}});
		}
		// enabling/disabling relevant actions
		targetsPage.newAction.setEnabled(true);
		targetsPage.removeAction.setEnabled(false);
		targetsPage.removeAllAction.setEnabled(true);
		targetsPage.buildAction.setEnabled(false);
		return true;
	}
	return false;

}
}
