package com.ibm.cpp.ui.internal.launchers;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.
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


public class CppRunLauncher implements ILauncherDelegate {

    private static String _directory = "";

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
   	   	ModelInterface api = ModelInterface.getInstance();
   	   	element = api.findResource((DataElement)element);
	      }
	
        if(!(element instanceof IProject || element instanceof IResource)) {
           System.out.println("CppRunLauncher.launch() error = selection is not an IProject or an IResource");
            return false;
        }
        IPath location = ((IResource)element).getLocation();
        IPath directory = location.removeLastSegments(1);
        _directory = directory.toString();
        System.out.println("CppRunLauncher.launch() _directory = " + _directory);

        IProject project = ((IResource)element).getProject();

        // display the wizard
        CppRunLauncherWizard w= new CppRunLauncherWizard();
        w.init(launcher, ILaunchManager.RUN_MODE, selection);
        WizardDialog wd= new WizardDialog(CppPlugin.getActiveWorkbenchWindow().getShell(), w);

        int rc = wd.open();

        if (rc == wd.OK) {
            return true;
        } else
            return false;
    }

    public void doLaunch(String program, String parameters) {

        System.out.println("CppRunLauncher.doLaunch()");

        ModelInterface api = ModelInterface.getInstance();

        String path = _directory;
        String command = program + " " + parameters;

        api.command(path, command, false);
    }

}
