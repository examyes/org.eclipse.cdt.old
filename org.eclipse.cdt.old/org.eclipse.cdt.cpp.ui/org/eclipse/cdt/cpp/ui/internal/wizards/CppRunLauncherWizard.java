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

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.resource.*;

import java.util.ArrayList;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
/**
 *	This is the Workbench's default project creation Wizard
 */
public class CppRunLauncherWizard extends Wizard implements ILaunchWizard
{
    private ILauncher                       _launcher;
    private IStructuredSelection            _selection;
    private Object                          _element;
    private CppRunLauncherWizardMainPage   _mainPage;
    private ProjectInfoWizardPage           _fProjectInfoWizardPage;
    private ParseWizardPage                 _parserWizardPage;
    private CppPlugin                       _plugin;
    private IProject                        _project;
    private String                          _programInvocation;


    public void addPages()
    {
	   super.addPages();
	
   	_mainPage = new CppRunLauncherWizardMainPage(_plugin.getLocalizedString("runLauncher"), _programInvocation);
    	_mainPage.setTitle(_plugin.getLocalizedString("runLauncher.Title"));
   	_mainPage.setDescription(_plugin.getLocalizedString("runLauncher.Description"));
   	this.addPage(_mainPage);
	
    }

    public CppRunLauncherWizardMainPage getMainPage()
    {
   	return _mainPage;
    }

    public String getParameters()
    {
   	return _mainPage.getParameters();
    }

    public boolean performFinish()
    {
   	_plugin = CppPlugin.getDefault();

   	_mainPage.finish();
    	String parameters = getParameters();
      getLauncher().doLaunch(_programInvocation, parameters);

      return true;		
    }


	protected CppRunLauncher getLauncher() {
		return (CppRunLauncher) _launcher.getDelegate();
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

      System.out.println("CppRunLauncherWizard - curentSelectionName = " + _programInvocation);
      _project = ((IResource)_element).getProject();
    }
}
