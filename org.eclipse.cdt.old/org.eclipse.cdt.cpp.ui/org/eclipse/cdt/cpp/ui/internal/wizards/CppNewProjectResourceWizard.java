package org.eclipse.cdt.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;
import org.eclipse.cdt.dstore.core.model.*;

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
    private PathWizardPage                  _pathWizardPage;
    private ParseWizardPage                 _parserWizardPage;
    private CppPlugin                       _plugin = CppPlugin.getDefault();

    public void addPages()
    {
	super.addPages();
	_mainPage = new CppWizardNewProjectCreationPage("C/C++ NewProjectPage");
	_mainPage.setTitle(_plugin.getLocalizedString("createProjectWizard.Title"));
	_mainPage.setDescription(_plugin.getLocalizedString("createProjectWizard.Description"));
	_mainPage.setImageDescriptor(_plugin.getImageDescriptor("newproject"));
	this.addPage(_mainPage);
	
	_fProjectInfoWizardPage = new ProjectInfoWizardPage(this);
 	this.addPage(_fProjectInfoWizardPage);	
	
	_pathWizardPage = new PathWizardPage(this);
 	this.addPage(_pathWizardPage);	

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
			
		    }
		
		// add parse behaviour
		ArrayList autoParse = _parserWizardPage._workbookPageParseBehaviour.getAutoParse();
		_plugin.writeProperty(project, "AutoParse", autoParse);

		ArrayList autoPersist = _parserWizardPage._workbookPageParseBehaviour.getAutoPersist();
		_plugin.writeProperty(project, "AutoPersist", autoPersist);

		// add parse quality
		ArrayList preferences = _parserWizardPage._workbookPageParseQuality.getQuality();
		_plugin.writeProperty(project, "ParseQuality", preferences);
		
		// add build history
		ArrayList builds = _fProjectInfoWizardPage._workbookPageBuildInvocation.getBuildInvocations();
		_plugin.writeProperty(project, "Build History", builds);

		// add clean history
		ArrayList cleans = _fProjectInfoWizardPage._workbookPageBuildInvocation.getCleanInvocations();
		_plugin.writeProperty(project, "Clean History", cleans);

		ArrayList variables = _fProjectInfoWizardPage._workbookPageEnvironment.getVariables();
		_plugin.writeProperty(project, "Environment", variables);

		ArrayList includePath        = _pathWizardPage.getIncludePath();
		_plugin.writeProperty(project, "Include Path", includePath);

		ArrayList externalSourcePath = _pathWizardPage.getExternalSourcePath();
		_plugin.writeProperty(project, "External Source", externalSourcePath);

		ArrayList libraries          = _pathWizardPage.getLibraries();
		_plugin.writeProperty(project, "Libraries", libraries);
		

		ModelInterface api = ModelInterface.getInstance();
		if (project instanceof Repository)
		    {
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
			api.initializeProject(project);
		    }
		else
		    {
			try
			    {
				// add build spec
				String builderName = "org.eclipse.cdt.cpp.ui.cppbuilder";
				IProjectDescription projectDescription =  project.getDescription();
				
				ICommand command = projectDescription.newCommand();
				command.setBuilderName(builderName);
				ICommand[] newCommands = new ICommand[1];
				newCommands[0] = command;
				projectDescription.setBuildSpec(newCommands);	
			
				// specify nature
				/*
				String[] natures = projectDescription.getNatureIds();
				String[] newNatures = new String[natures.length + 1];
				System.arraycopy(natures, 0, newNatures, 0, natures.length);
				newNatures[natures.length] = "org.eclipse.cdt.cpp.ui.cppnature";
				*/

				String[] newNatures = new String[1];
				newNatures[0] = "org.eclipse.cdt.cpp.ui.cppnature";
				projectDescription.setNatureIds(newNatures);
				project.setDescription(projectDescription, null);

			    }
			catch (CoreException e)
			    {
				System.out.println(e);
			    }
		    }

		// refresh view
		openPerspective(project);
		api.openProject(project, getShell());	
		
		return true;
	    }
	return false;
    }


    private void openPerspective(IProject project)
    {
    IWorkbench workbench = _plugin.getWorkbench();
    IWorkspace workspace = _plugin.getPluginWorkspace();
	IWorkbenchWindow dw = workbench.getActiveWorkbenchWindow();
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
			persp = workbench.openPage("org.eclipse.cdt.cpp.ui.CppPerspective", workspace.getRoot(), 0);
		    }
	    }
        catch (WorkbenchException e)
	    {
	    }

	
	String navID = "org.eclipse.cdt.cpp.ui.CppProjectsViewPart";
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
