package com.ibm.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.launchers.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.dstore.ui.resource.*;
import com.ibm.dstore.core.model.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.core.resources.*;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.ILaunchWizard;

import com.ibm.debug.launch.PICLLoadInfo;


import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.resource.*;

import java.util.ArrayList;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
/**
 *	This is the Workbench's default project creation Wizard
 */
public class CppLoadLauncherWizard extends Wizard implements ILaunchWizard
{
    private ILauncher                       _launcher;
    private Object                          _element;
    private CppLoadLauncherWizardMainPage   _mainPage;
    private ProjectInfoWizardPage           _fProjectInfoWizardPage;
    private ParseWizardPage                 _parserWizardPage;
    private CppPlugin                       _plugin;
    private IProject                        _project;
    private String                          _currentSelectionName;
    private DataElement                     _directory;

    private ModelInterface                  _api;

    public void addPages()
    {
	super.addPages();
	
   	_plugin = CppPlugin.getDefault();

   	_mainPage = new CppLoadLauncherWizardMainPage(_plugin.getLocalizedString("debugLauncher"), _currentSelectionName, _directory);
    	_mainPage.setTitle(_plugin.getLocalizedString("debugLauncher.Title"));
   	_mainPage.setDescription(_plugin.getLocalizedString("debugLauncher.Description"));
   	this.addPage(_mainPage);	

	_api = _plugin.getModelInterface();
    }

    public CppLoadLauncherWizardMainPage getMainPage()
    {
   	return _mainPage;
    }
/*
    public boolean debugInitialization()
    {
   	return _mainPage.debugInitialization();
    }
*/

    public String getParameters()
    {
   	return _mainPage.getParameters();
    }

    public String getWorkingDirectory()
    {
   	return _mainPage.getWorkingDirectory();
    }

    public boolean performFinish()
    {
	
   	_mainPage.finish();
      //	boolean debugInitialization = debugInitialization();
    	String parameters = getParameters();
    	String workingDirectory = getWorkingDirectory();
	
	PICLLoadInfo loadInfo = new PICLLoadInfo();
   String qualifiedFileName = "";

	if (_element instanceof DataElement)
	    {
         qualifiedFileName = ((DataElement)_element).getSource();
         //System.out.println("CppLoadLauncherWizard:performFinish() - qualifiedFileName = " + qualifiedFileName);
		IFile file = (IFile)_api.findFile(((DataElement)_element).getSource());
		if (file == null)
		    {
			DataElement projectElement = _api.getProjectFor((DataElement)_element);
			IProject project = _api.findProjectResource(projectElement);
			file = new FileResourceElement((DataElement)_element, project);
			_api.addNewFile(file);			
		    }

		loadInfo.setResource(file);
	    }
	else
	    {
		loadInfo.setResource(_element); // this doesn't seem to do anything
	    }

	loadInfo.setLauncher(_launcher);
//	loadInfo.setProgramName(_currentSelectionName);
	loadInfo.setProgramName(qualifiedFileName);
   //System.out.println("CppLoadLauncherWizard:performFinish() - _currentSelectionName = " + _currentSelectionName);
	loadInfo.setProgramParms(parameters);
	
	int startupBehaviour;
	
//	if (debugInitialization)
//	    {
//		startupBehaviour = loadInfo.DEBUG_INITIALIZATION;
//	    }
//	else
//	    {
		startupBehaviour = loadInfo.RUN_TO_MAIN;
//	    }
	
	loadInfo.setStartupBehaviour(startupBehaviour);
	
	
	getLauncher().doLaunch(loadInfo, workingDirectory);

      return true;		
    }


    protected CppLoadLauncher getLauncher()
    {
	return (CppLoadLauncher) _launcher.getDelegate();
    }

    public void init(ILauncher launcher, String mode, IStructuredSelection selection)
    {
	if (selection.getFirstElement() instanceof DataElement)
	    {
		init(launcher, mode, (DataElement)selection.getFirstElement());		
	    }
    }

    public void init(ILauncher launcher, String mode, DataElement resource)
    {
   	_launcher = launcher;
   	_element = resource;
	
//   	_currentSelectionName = ((DataElement)_element).getName();
   	_currentSelectionName = ((DataElement)_element).getSource();
   	_directory = ((DataElement)_element).getParent();
    }
}
