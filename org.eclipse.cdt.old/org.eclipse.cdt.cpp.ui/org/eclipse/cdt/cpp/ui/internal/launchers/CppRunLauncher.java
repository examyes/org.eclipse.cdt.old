package com.ibm.cpp.ui.internal.launchers;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


import com.ibm.dstore.core.model.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILauncherDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.WorkspaceSourceLocator;
import com.ibm.debug.launch.PICLDaemonInfo;
import com.ibm.debug.launch.PICLLoadInfo;

import com.ibm.cpp.ui.internal.CppPlugin;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.wizards.*;


import java.io.IOException;


public class CppRunLauncher implements ILauncherDelegate
{
    private static DataElement _directory;
    private static DataElement _executable;
    private static ModelInterface _api;

    public CppRunLauncher()
    {
	_api = ModelInterface.getInstance();
    }

    /**
     * @see ILauncherDelegate#launch(Object[], String, ILauncher)
     */
    public boolean launch(Object[] elements, String mode, ILauncher launcher) {

        // Get the selection and check if valid
        StructuredSelection selection = new StructuredSelection(elements);
        if(selection == null) {
           System.out.println("CppRunLauncher.launch() error = selection is null");
            return false;
        }
        Object element = selection.getFirstElement();

	if (element instanceof DataElement)
	    {
		_executable = (DataElement)element;
		if (!_executable.getType().equals("file"))
		    {
			_executable = null;
			_directory = null;
			return false;
		    }
		_directory = _executable.getParent();
	    }	
        else if (element instanceof IProject || element instanceof IResource)
	    {
		_executable = _api.findResourceElement((IResource)element);
		if (_executable == null)
		    {
			IResource resource = (IResource)element;
			IResource parentRes = resource.getParent();
			
			CppPlugin plugin = CppPlugin.getDefault();
			DataStore dataStore = plugin.getCurrentDataStore();

			_directory = dataStore.createObject(null, "directory", parentRes.getName(),
							    parentRes.getLocation().toString());

			_executable = dataStore.createObject(_directory, "file", resource.getName(),
							     resource.getLocation().toString());
			
		    }
		else
		    {
			_directory = _executable.getParent();
		    }
	    }
	else
	    {
		_executable = null;
		_directory = null;
		return false;
	    }

        // display the wizard
        CppRunLauncherWizard w= new CppRunLauncherWizard();
        w.init(launcher, ILaunchManager.RUN_MODE, _executable);
        WizardDialog wd= new WizardDialog(CppPlugin.getActiveWorkbenchWindow().getShell(), w);

        int rc = wd.open();

        if (rc == wd.OK) {
            return true;
        } else
            return false;
    }

    public void doLaunch(String program, String parameters, String workingDirectory)
    {

        System.out.println("CppRunLauncher.doLaunch()");

        ModelInterface api = ModelInterface.getInstance();

        String command = program + " " + parameters;

        if (workingDirectory != "")
        {
            System.out.println("CppRunLauncher:doLaunch() - workingDirectory = " + workingDirectory);
         	_api.invoke(workingDirectory, command, false);
        }
        else
        {
           System.out.println("CppLoadLauncher:doLaunch() - _directory = " + _directory);
            _api.invoke(_directory, command, false);
        }

    }

}
