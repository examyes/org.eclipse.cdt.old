package com.ibm.cpp.ui.internal.wizards;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.
 */

import com.ibm.cpp.ui.internal.launchers.*;
import com.ibm.cpp.ui.internal.*;
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
    private CppAttachLauncherWizardMainPage   _mainPage;
    private ProjectInfoWizardPage           _fProjectInfoWizardPage;
    private ParseWizardPage                 _parserWizardPage;
    private CppPlugin                       _plugin;
    private IProject                        _project;
    private String                          _programInvocation;


    public void addPages()
    {
	   super.addPages();
	
   	_mainPage = new CppAttachLauncherWizardMainPage(_plugin.getLocalizedString("debugAttachLauncher"), _programInvocation);
    	_mainPage.setTitle(_plugin.getLocalizedString("debugAttachLauncher.Title"));
   	_mainPage.setDescription(_plugin.getLocalizedString("debugAttachLauncher.Description"));
   	this.addPage(_mainPage);
	
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

         /**/
      PICLAttachInfo attachInfo = new PICLAttachInfo();

      attachInfo.setResource(_element); // this doesn't seem to do anything
      attachInfo.setLauncher(_launcher);
      System.out.println("CppAttachLauncherWizard - processID in attachInfo = " + processID);
      attachInfo.setProcessID(processID);

      getLauncher().doLaunch(attachInfo);
         /**/

      return true;		
    }


	protected CppAttachLauncher getLauncher() {
		return (CppAttachLauncher) _launcher.getDelegate();
	}

    /**
     *	@param selection org.eclipse.jface.viewer.IStructuredSelection
     */
    public void init(ILauncher launcher, String mode, IStructuredSelection currentSelection) {
   	_launcher = launcher;
   	_selection = currentSelection;
      _element = _selection.getFirstElement();

      String currentSelectionName = ((IResource)_element).getName();
      String extension = ((IResource)_element).getFileExtension();
      if (extension != null)
      {
   		int indexOfExtension = currentSelectionName.lastIndexOf(extension);
   		_programInvocation = currentSelectionName.substring(0, indexOfExtension - 1);
      }
      else
      {
   		_programInvocation = currentSelectionName;		      		
      }

      System.out.println("CppAttachLauncherWizard - programInvocation = " + _programInvocation);
      _project = ((IResource)_element).getProject();
    }
}
