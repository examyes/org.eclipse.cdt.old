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
import com.ibm.debug.launch.PICLAttachInfo;

import com.ibm.cpp.ui.internal.CppPlugin;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.wizards.*;


import java.io.IOException;


public class CppAttachLauncher implements ILauncherDelegate {

    private static String _directory = "";

    /**
     * @see ILauncherDelegate#launch(Object[], String, ILauncher)
     */
    public boolean launch(Object[] elements, String mode, ILauncher launcher) {

        // Get the selection and check if valid
        StructuredSelection selection = new StructuredSelection(elements);
        if(selection == null) {
           System.out.println("CppAttachLauncher.launch() error = selection is null");
            return false;
        }
        Object element = selection.getFirstElement();
	
	if (element instanceof DataElement)
	    {
		ModelInterface api = ModelInterface.getInstance();
		element = api.findResource((DataElement)element);
	    }
	
        if(!(element instanceof IProject || element instanceof IResource)) {
           System.out.println("CppAttachLauncher.launch() error = selection is not an IProject or an IResource");
            return false;
        }
        IPath location = ((IResource)element).getLocation();
        IPath directory = location.removeLastSegments(1);
        _directory = directory.toString();
        System.out.println("CppAttachLauncher.launch() _directory = " + _directory);

        IProject project = ((IResource)element).getProject();

        // display the wizard
        CppAttachLauncherWizard w= new CppAttachLauncherWizard();
        w.init(launcher, ILaunchManager.DEBUG_MODE, selection);
        WizardDialog wd= new WizardDialog(CppPlugin.getActiveWorkbenchWindow().getShell(), w);

        int rc = wd.open();

        if (rc == wd.OK) {
            return true;
        } else
            return false;
    }

    public void doLaunch(PICLAttachInfo attachInfo) {

        WorkspaceSourceLocator sourceLocator = new WorkspaceSourceLocator();

           System.out.println("CppAttachLauncher.doLaunch()");

        // If we can get a project from the selection, tell source locator
        Object resource = attachInfo.getResource();
        if(resource != null) {
            IProject curProject = null;
            if(resource instanceof IProject)
                curProject = (IProject)resource;
            else if(resource instanceof IResource)
                curProject = ((IResource)resource).getProject();

	    if (curProject != null)
		sourceLocator.setHomeProject(curProject);
        }

	attachInfo.setWorkspaceSourceLocator(sourceLocator);

        PICLDaemonInfo daemonInfo = PICLDebugPlugin.getDefault().launchDaemon(attachInfo);
        if(daemonInfo == null)
            return;

        launchEngine(daemonInfo);
    }



/*
        String command = "java com.ibm.debug.gdb.Gdb -qhost=localhost -quiport=" +
                         (new Integer(daemonInfo.getPort())).toString() +
                           " -startupKey=" +
                         (new Integer(daemonInfo.getKey())).toString();

        ModelInterface api = ModelInterface.getInstance();
        String path = _directory;

        api.command(path, command, false);
*/

    protected void launchEngine(PICLDaemonInfo daemonInfo)
    {
	ModelInterface api = ModelInterface.getInstance();
	String port = new Integer(daemonInfo.getPort()).toString();
	String key  = new Integer(daemonInfo.getKey()).toString();

	api.debug(_directory, port, key);
    }

}
