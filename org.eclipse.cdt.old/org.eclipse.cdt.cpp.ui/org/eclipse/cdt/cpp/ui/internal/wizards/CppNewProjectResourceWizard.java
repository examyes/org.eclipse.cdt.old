package com.ibm.cpp.ui.internal.wizards;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.vcm.*;
import com.ibm.dstore.core.model.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.core.resources.*;

import org.eclipse.ui.views.contentoutline.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.resource.*;

import java.util.ArrayList;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
/**
 *	This is the Workbench's default project creation Wizard
 */
public class CppNewProjectResourceWizard extends Wizard implements INewWizard 
{
    private IWorkbench                      _desktop;
    private IStructuredSelection            _selection;
    private CppWizardNewProjectCreationPage _mainPage;
    private ProjectInfoWizardPage           _fProjectInfoWizardPage;
    private ParseWizardPage                 _parserWizardPage;
    private CppPlugin                       _plugin;

    public void addPages() 
    {
	super.addPages();
	_mainPage = new CppWizardNewProjectCreationPage("C/C++ NewProjectPage");
	_mainPage.setTitle("C/C++ Project");
	_mainPage.setDescription("Create a new project resource.");
	this.addPage(_mainPage);
	
	_fProjectInfoWizardPage = new ProjectInfoWizardPage(this);
 	this.addPage(_fProjectInfoWizardPage);	

	_parserWizardPage = new ParseWizardPage(this);
 	this.addPage(_parserWizardPage);	
    }

    public CppWizardNewProjectCreationPage getMainPage()
    {
	return _mainPage;
    }

    public boolean isRemote()
    {
	return _mainPage.isRemote();
    }

    public IProject getProject()
    {
	return _mainPage.getNewProject();
    }

    public boolean performFinish()
    {
      _plugin = CppPlugin.getDefault();
      _mainPage.finish();

	IProject project = getProject();
	if (project != null)
	    {
		// add C++ project indicator
		IPath newPath = project.getFullPath();
		QualifiedName indicatorFile = new QualifiedName("C++ Project", newPath.toString());
		try
		    {
			project.setPersistentProperty(indicatorFile, "yes");
		    }
		catch (CoreException ce)
		    {
			System.out.println("CppNewProjectResourceWizard setPersistentProperty C++ Project CoreException: " +ce);
		    }
		
		// add parse paths
		ArrayList paths = _parserWizardPage._workbookPageParsePath._pathControl.getPaths();
		_plugin.writeProperty(project, "Include Path", paths);
		
		// add parse quality
		ArrayList preferences = _parserWizardPage._workbookPageParseQuality.getQuality();
		_plugin.writeProperty(project, "ParseQuality", preferences);      
		
		// add build history
		ArrayList builds = _fProjectInfoWizardPage._workbookPageBuildInvocation.getInvocations();
		_plugin.writeProperty(project, "Build History", builds);

		ArrayList variables = _fProjectInfoWizardPage._workbookPageEnvironment.getVariables();
		_plugin.writeProperty(project, "Environment", variables);
		ModelInterface api = ModelInterface.getInstance();
		if (project instanceof Repository)
		    {
                     api.openProject(project);

			QualifiedName mountFile = new QualifiedName("Mount Point", newPath.toString());
			try
			    {
				String mountLocation = _mainPage.getRemoteMountPoint();
				project.setPersistentProperty(mountFile, mountLocation);
			    }
			catch (CoreException ce)
			    {
				System.out.println("CppNewProjectResourceWizard setPersistenProperty C++ Project CoreException: " +ce);
			    }
		    }
		else
		    {
			// add build spec
			try 
			    { 
				String builderName = "com.ibm.cpp.ui.cppbuilder";
				IProjectDescription projectDescription =  project.getDescription();
				
				ICommand command = projectDescription.newCommand();
				command.setBuilderName(builderName);
				ICommand[] newCommands = new ICommand[1];
				newCommands[0] = command;
				projectDescription.setBuildSpec(newCommands);
				project.setDescription(projectDescription, null);
			    }
			catch (CoreException ce)
			    {
				System.out.println("CppNewProjectResourceWizard project.setBuildSpec CoreException " +ce);
			    }

			// tell parser miner to open the project
			
			api.openProject(project);	       
		    }
		
		// refresh view
		openPerspective(project);
		
		return true;
	    }
	return false;
    }
    
    
    private void openPerspective(IProject project)
    {
        IWorkspace workspace = _plugin.getPluginWorkspace();
	IWorkbenchWindow dw = _plugin.getWorkbench().getActiveWorkbenchWindow();
	IWorkbenchPage persp = null;
	
	IWorkbenchPage[] perspectives = dw.getPages();
	
	try
	    {
		for (int i = 0; i < perspectives.length; i++)
		    {
			IWorkbenchPage aPersp = perspectives[i];
			String layoutId = aPersp.getLabel();
			
			if (layoutId.equals("Workspace : C/C++ Perspective"))
			    {
				persp = aPersp;	
				dw.setActivePage(persp);		
			    }	
		    }
		
		
		if (persp == null)
		    {
			persp = dw.openPage("com.ibm.cpp.ui.internal.views.CppPerspective", workspace.getRoot());
		    }
	    }
        catch (WorkbenchException e)
	    {
	    }

	
	//String navID = IPageLayout.ID_RES_NAV;
	String navID = "com.ibm.cpp.ui.internal.views.CppProjectsViewPart";

	// eventually this will be gone
	if (project instanceof Repository)
	    {
		navID = "com.ibm.cpp.ui.internal.views.RemoteProjectViewPart";
		((Repository)project).refresh();
	    }

	final IViewPart viewPart = persp.findView(navID);
	if (viewPart != null)
	    {
		persp.bringToTop(viewPart);

		if (viewPart instanceof ISetSelectionTarget)
		    {
			final ISelection selection = new StructuredSelection(project);
			getShell().getDisplay().asyncExec(new Runnable()
			    {
				public void run()
				{
				    ((ISetSelectionTarget)viewPart).selectReveal(selection);
				}
			    });
		    }
	    }
    }
    
    public IDialogSettings getDialogSettings() 
    {
	return _plugin.getDialogSettings();
    }
    
    /**
     *	Answer self's Wizard, optionally utilizing the passed
     *	Workbench and current resource selection.  Since in this case
     *	self is the Wizard, simply answer self
     *
     *	@return org.eclipse.jface.wizards.Wizard
     *	@param desktop com.ibm.itp.ui.desktop.Workbench
     *	@param selection org.eclipse.jface.viewer.IStructuredSelection
     */
    public void init(IWorkbench aWorkbench,IStructuredSelection currentSelection) {
	_desktop = aWorkbench;
	_selection = currentSelection;
	//***  We need to put here our own image for a C++ project
	//	setDefaultPageImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_NEWPRJ_WIZ));
    }
}
