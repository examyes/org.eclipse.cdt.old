package com.ibm.cpp.ui.internal.wizards;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.
 */

import com.ibm.cpp.ui.internal.launchers.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;
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
    private IStructuredSelection            _selection;
    private Object                          _element;
    private CppLoadLauncherWizardMainPage   _mainPage;
    private ProjectInfoWizardPage           _fProjectInfoWizardPage;
    private ParseWizardPage                 _parserWizardPage;
    private CppPlugin                       _plugin;
    private IProject                        _project;
    private String                          _currentSelectionName;


    public void addPages()
    {
	   super.addPages();
	
   	_mainPage = new CppLoadLauncherWizardMainPage(_plugin.getLocalizedString("debugLauncher"), _currentSelectionName);
    	_mainPage.setTitle(_plugin.getLocalizedString("debugLauncher.Title"));
   	_mainPage.setDescription(_plugin.getLocalizedString("debugLauncher.Description"));
   	this.addPage(_mainPage);
	
    }

    public CppLoadLauncherWizardMainPage getMainPage()
    {
   	return _mainPage;
    }

    public boolean debugInitialization()
    {
   	return _mainPage.debugInitialization();
    }

    public String getParameters()
    {
   	return _mainPage.getParameters();
    }

    public boolean performFinish()
    {
   	_plugin = CppPlugin.getDefault();

   	_mainPage.finish();
      boolean debugInitialization = debugInitialization();
    	String parameters = getParameters();

         /**/
      PICLLoadInfo loadInfo = new PICLLoadInfo();

      loadInfo.setResource(_element); // this doesn't seem to do anything
      loadInfo.setLauncher(_launcher);
      loadInfo.setProgramName(_currentSelectionName);
      loadInfo.setProgramParms(parameters);

      int startupBehaviour;

      if (debugInitialization)
      {
         startupBehaviour = loadInfo.DEBUG_INITIALIZATION;
      }
      else
      {
         startupBehaviour = loadInfo.RUN_TO_MAIN;
      }

      loadInfo.setStartupBehaviour(startupBehaviour);


      getLauncher().doLaunch(loadInfo);
         /**/

      return true;		
    }


	protected CppLoadLauncher getLauncher() {
		return (CppLoadLauncher) _launcher.getDelegate();
	}

    /**
     *	@param selection org.eclipse.jface.viewer.IStructuredSelection
     */
    public void init(ILauncher launcher, String mode, IStructuredSelection currentSelection) {
   	_launcher = launcher;
   	_selection = currentSelection;
	_element = _selection.getFirstElement();

	if (_element instanceof DataElement)
	    {
		ModelInterface api = ModelInterface.getInstance();
		_element = api.findResource((DataElement)_element);
	    }

	if (_element instanceof IResource)
	    {
		_currentSelectionName = ((IResource)_element).getName();
		System.out.println("CppLoadLauncherWizard - curentSelectionName = " + _currentSelectionName);
		_project = ((IResource)_element).getProject();
	    }

    }
}
