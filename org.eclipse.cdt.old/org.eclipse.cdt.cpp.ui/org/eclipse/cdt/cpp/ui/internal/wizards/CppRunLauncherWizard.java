package org.eclipse.cdt.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.launchers.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.dstore.core.model.*;

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
    private DataElement                     _directory;
    private ModelInterface                  _api;
    private boolean                         _projectIsClosed = false;
    private boolean                         _noSelection = false;

    public void addPages()
    {
	   super.addPages();
        if (_projectIsClosed)
        {
            displayMessageDialog(_plugin.getLocalizedString("runLauncher.Error.projectClosed"));
            return;
        }
        if (_noSelection)
        {
            displayMessageDialog(_plugin.getLocalizedString("runLauncher.Error.noSelection"));
            return;
        }
   	_mainPage = new CppRunLauncherWizardMainPage(_plugin.getLocalizedString("runLauncher"), _programInvocation, _directory);
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

    public String getWorkingDirectory()
    {
   	return _mainPage.getWorkingDirectory();
    }

    public boolean performFinish()
    {
   	_plugin = CppPlugin.getDefault();
        _api = _plugin.getModelInterface();
   	_mainPage.finish();
    	String parameters = getParameters();
    	String workingDirectory = getWorkingDirectory();
   	getLauncher().doLaunch(_programInvocation, parameters, workingDirectory);
      _projectIsClosed = false;
   	return true;		
    }


    protected CppRunLauncher getLauncher()
    {
   	return (CppRunLauncher) _launcher.getDelegate();
    }

    public void init(ILauncher launcher, String mode, IStructuredSelection selection)
    {
        DataElement dataElement = null;
        Object element = selection.getFirstElement();
        IProject project;
        _noSelection = false;

        _plugin = CppPlugin.getDefault();
        _api = _plugin.getModelInterface();

       	if (element instanceof DataElement)
      	{
           dataElement = (DataElement)element;		
           DataElement projectElement = _api.getProjectFor(dataElement);
           project = _api.findProjectResource(projectElement);
           if (!project.isOpen())
           {
              _projectIsClosed = true;
              return;
           }
           init(launcher, mode, dataElement);
      	}
        else if (element instanceof IProject || element instanceof IResource)
        {
           dataElement = _api.findResourceElement((IResource)element);
           project = ((IResource)element).getProject();
           if (!project.isOpen())
           {
              _projectIsClosed = true;
              return;
           }
           init(launcher, mode, dataElement);
        }
        else
        {
           _noSelection = true;
           return;
        }

    }

    public void init(ILauncher launcher, String mode, DataElement resource)
    {
   	_launcher = launcher;
   	_element = resource;
   	_programInvocation = ((DataElement)_element).getSource();
   	_directory = ((DataElement)_element).getParent();
    }
 /**
     *	Display an error dialog with the specified message.
     *
     *	@param message java.lang.String
     */
    protected void displayMessageDialog(String message)
    {
	     MessageDialog.openError(CppPlugin.getActiveWorkbenchWindow().getShell(),_plugin.getLocalizedString("runLauncher.Error.Title"),message);
    }
}
