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

import com.ibm.debug.launch.PICLAttachInfo;


import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.resource.*;

import java.util.ArrayList;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
/**
 *	This is the Workbench's default project creation Wizard
 */
public class CppAttachLauncherWizard extends Wizard implements ILaunchWizard
{
    private ILauncher                       _launcher;
    private IStructuredSelection            _selection;
    private Object                          _element;
    private CppAttachLauncherWizardMainPage _mainPage;
    private ProjectInfoWizardPage           _fProjectInfoWizardPage;
    private ParseWizardPage                 _parserWizardPage;
    private CppPlugin                       _plugin;
    private IProject                        _project;
    private String                          _programInvocation;
    private String                          _currentSelectionName;
    
    private ModelInterface                  _api;

    public void addPages()
    {
	super.addPages();
	
	_plugin = CppPlugin.getDefault();

   	_mainPage = new CppAttachLauncherWizardMainPage(_plugin.getLocalizedString("debugAttachLauncher"), _programInvocation);
    	_mainPage.setTitle(_plugin.getLocalizedString("debugAttachLauncher.Title"));
   	_mainPage.setDescription(_plugin.getLocalizedString("debugAttachLauncher.Description"));
   	this.addPage(_mainPage);
	
	_api = _plugin.getModelInterface();
    }

    public CppAttachLauncherWizardMainPage getMainPage()
    {
   	return _mainPage;
    }

    public String getProcessID()
    {
   	return _mainPage.getProcessID();
    }

    public boolean performFinish()
    {
   	_plugin = CppPlugin.getDefault();

   	_mainPage.finish();
    	String processID = getProcessID();

      PICLAttachInfo attachInfo = new PICLAttachInfo();

      if (_element instanceof DataElement)
	  {
	      IFile file = (IFile)_api.findFile(((DataElement)_element).getSource());
	      if (file == null)
		  {
		      DataElement projectElement = _api.getProjectFor((DataElement)_element);
		      IProject project = _api.findProjectResource(projectElement);
		      file = new FileResourceElement((DataElement)_element, project);
		      _api.addNewFile(file);			
		  }
	      
	      attachInfo.setResource(file);
	  }
      else
	  {
	      attachInfo.setResource(_element);
	  }
      
      attachInfo.setLauncher(_launcher);
      attachInfo.setProcessID(processID);

      getLauncher().doLaunch(attachInfo);

      return true;		
    }


	protected CppAttachLauncher getLauncher() {
		return (CppAttachLauncher) _launcher.getDelegate();
	}

    /**
     *	@param selection org.eclipse.jface.viewer.IStructuredSelection
     */
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

	_currentSelectionName = ((DataElement)_element).getName();

	System.out.println("CppAttachLauncherWizard - programInvocation = " + _currentSelectionName);
    }
}
