package com.ibm.cpp.ui.internal.launchers;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.
 */


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

import com.ibm.cpp.ui.internal.api.*;


import java.io.IOException;


public class CppLoadLauncher implements ILauncherDelegate {

    private static String _directory = "";

    /**
     * @see ILauncherDelegate#launch(Object[], String, ILauncher)
     */
    public boolean launch(Object[] elements, String mode, ILauncher launcher) {

        // Get the selection and check if valid
        StructuredSelection selection = new StructuredSelection(elements);
        if(selection == null) {
           System.out.println("CppLoadLauncher.launch() error = selection is null");
            return false;
        }
        Object element = selection.getFirstElement();
        if(!(element instanceof IProject || element instanceof IResource)) {
           System.out.println("CppLoadLauncher.launch() error = selection is not an IProject or an IResource");
            return false;
        }
        IPath location = ((IResource)element).getLocation();
        IPath directory = location.removeLastSegments(1);
        _directory = directory.toOSString();
        System.out.println("CppLoadLauncher.launch() _directory = " + _directory);

/*
        // display the wizard
        PICLLoadWizard w= new PICLLoadWizard();
        w.init(launcher, ILaunchManager.DEBUG_MODE, selection);
        WizardDialog wd= new WizardDialog(PICLDebugPlugin.getActiveWorkbenchWindow().getShell(), w);

        int rc = wd.open();

        if (rc == wd.OK) {
            return true;
        } else
            return false;
*/
        PICLLoadInfo loadInfo = new PICLLoadInfo();

        loadInfo.setResource(element);
        loadInfo.setLauncher(launcher);
        String name = ((IResource)element).getName();
        System.out.println("CppLoadLauncher.launch() program name = " + name);
        loadInfo.setProgramName(name);
        loadInfo.setProgramParms("3");
        int startupBehaviour = loadInfo.RUN_TO_MAIN;
        loadInfo.setStartupBehaviour(startupBehaviour);

        doLaunch(loadInfo);
            return true;

    }

    protected void doLaunch(PICLLoadInfo loadInfo) {

        WorkspaceSourceLocator sourceLocator = new WorkspaceSourceLocator();

        // If we can get a project from the selection, tell source locator
        Object resource = loadInfo.getResource();
        if(resource != null) {
            IProject curProject = null;
            if(resource instanceof IProject)
                curProject = (IProject)resource;
            else if(resource instanceof IResource)
                curProject = ((IResource)resource).getProject();

            if (curProject != null)
                sourceLocator.setHomeProject(curProject);
        }

        loadInfo.setWorkspaceSourceLocator(sourceLocator);

        PICLDaemonInfo daemonInfo = PICLDebugPlugin.getDefault().launchDaemon(loadInfo);
        if(daemonInfo == null)
            return;

        launchEngine(daemonInfo);
    }

    protected void launchEngine(PICLDaemonInfo daemonInfo) {



//        String command = "irmtdbgc -qhost=localhost -quiport=" +
//        String command = "irmtdbgg -qhost=localhost -quiport=" +
/*
        String command = "java com.ibm.debug.gdb.Gdb -qhost=localhost -quiport=" +
                         (new Integer(daemonInfo.getPort())).toString() +
                           " -startupKey=" +
                         (new Integer(daemonInfo.getKey())).toString();

        ModelInterface api = ModelInterface.getInstance();
        String path = _directory;

        api.command(path, command, false);
*/

	ModelInterface api = ModelInterface.getInstance();
	String port = new Integer(daemonInfo.getPort()).toString();
	String key  = new Integer(daemonInfo.getKey()).toString();

	System.out.println("call to debug");
	api.debug(_directory, port, key);

        /*
        try {
            Runtime.getRuntime().exec(command);
        } catch(SecurityException e) {
            //displayError(LAUNCHER+".engineLaunchSecurityError");
        } catch(IOException e) {
            //displayError(LAUNCHER+".engineLaunchError");
        }
        */

    }
    /*
    protected void displayError(String msgCode) {
        String title = PICLUtils.getResourceString(LAUNCHER+".errorTitle");
        String msg = PICLUtils.getResourceString(msgCode);
        MessageDialog.openError(PICLDebugPlugin.getActiveWorkbenchShell(), title, msg);
    }
    */
}
