package com.ibm.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
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


    protected CppRunLauncher getLauncher() 
    {
	return (CppRunLauncher) _launcher.getDelegate();
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
	_programInvocation = ((DataElement)_element).getName();
    }


}


