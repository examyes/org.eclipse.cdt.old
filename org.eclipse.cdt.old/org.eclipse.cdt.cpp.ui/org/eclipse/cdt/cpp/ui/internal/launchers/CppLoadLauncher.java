package org.eclipse.cdt.cpp.ui.internal.launchers;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


import org.eclipse.cdt.dstore.core.model.*;

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

import com.ibm.debug.daemon.IOldDaemonSupport;
import com.ibm.debug.daemon.CoreDaemon;
import com.ibm.debug.daemon.DaemonLauncherDelegate;
import com.ibm.debug.daemon.DaemonSocketConnection;
import com.ibm.debug.internal.picl.PICLDebugTarget;

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.wizards.*;


import java.io.IOException;


public class CppLoadLauncher implements ILauncherDelegate, IOldDaemonSupport
{
    private static DataElement _directory;
    private static DataElement _executable;
    private static ModelInterface _api;
    private static CppPlugin   _plugin;
    private static Object[] _elements;


    public CppLoadLauncher()
    {
	_api = ModelInterface.getInstance();
      _plugin = CppPlugin.getDefault();
    }

public String getLaunchMemento(Object obj)
	{
		return null;	
	}
	
	public Object getLaunchObject(String mem)
	{
		return null;	
	}
    public boolean launch(Object[] elements, String mode, ILauncher launcher)
    {
        _elements = elements;
        // Get the selection and check if valid
        StructuredSelection selection = new StructuredSelection(elements);
        if(selection == null)
	    {
         displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.noSelection"));
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
               displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.notExecutable"));
			return false;
	    }

		_directory = _executable.getParent();
    }	
    else if (element instanceof IProject || element instanceof IResource)
    {
		_executable = _api.findResourceElement((IResource)element);
		if (_executable == null)
	    {
         IProject project = ((IResource)element).getProject();
         if (_plugin.isCppProject(project))
         {
   			IResource resource = (IResource)element;
	   		IResource parentRes = resource.getParent();
			
   			DataStore dataStore = _plugin.getCurrentDataStore();
	   		_directory = dataStore.createObject(null, "directory", parentRes.getName(),
 	  						    parentRes.getLocation().toString());

	   		_executable = dataStore.createObject(_directory, "file", resource.getName(),
							     resource.getLocation().toString());
         }
         else
         {
            displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.notCppProject"));
            return false;
         }

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
        CppLoadLauncherWizard w= new CppLoadLauncherWizard();
        w.init(launcher, ILaunchManager.DEBUG_MODE, _executable);

        WizardDialog wd= new WizardDialog(CppPlugin.getActiveWorkbenchWindow().getShell(), w);
	
        int rc = wd.open();

        if (rc == wd.OK) {
            return true;
        } else
            return false;
    }

    public void doLaunch(PICLLoadInfo loadInfo, String workingDirectory)
    {
        CppSourceLocator sourceLocator = null;


        //ensure the daemon is listening
        int port = DaemonLauncherDelegate.launchDaemon(_elements);
        if (port < 0)
            return;

	
	if (_executable != null)
	    {
		DataElement projectElement = _api.getProjectFor(_executable);
		if (projectElement != null)
		    {
			sourceLocator = new CppSourceLocator(projectElement);
			loadInfo.setWorkspaceSourceLocator(sourceLocator);
		    }
	   }
	
      PICLDebugTarget target = new  PICLDebugTarget(loadInfo, loadInfo.getEngineConnectionInfo());
      int key = CoreDaemon.generateKey();
      CoreDaemon.storeDebugTarget(target, key);

//      PICLDaemonInfo daemonInfo = PICLDebugPlugin.getDefault().launchDaemon(loadInfo);
      PICLDaemonInfo daemonInfo = new PICLDaemonInfo(key,
                                      new Integer(loadInfo.getEngineConnectionInfo().getConduit()).intValue());
	
      if(daemonInfo == null)
         return;
	
      launchEngine(daemonInfo, workingDirectory);
    }

    protected void launchEngine(PICLDaemonInfo daemonInfo, String workingDirectory)
    {
	String port = new Integer(daemonInfo.getPort()).toString();
	String key  = new Integer(daemonInfo.getKey()).toString();


      if (workingDirectory != "")
      {
       	_api.debug(workingDirectory, port, key);
      }
      else
      {
       	_api.debug(_directory, port, key);
      }
    }


    /**
     *	Display an error dialog with the specified message.
     *
     *	@param message java.lang.String
     */
    protected void displayMessageDialog(String message)
    {
	     MessageDialog.openError(CppPlugin.getActiveWorkbenchWindow().getShell(),_plugin.getLocalizedString("loadLauncher.Error.Title"),message);
    }


    /*
	 * @see IOldDaemonSupport#setSocket(DaemonSocketConnection)
	 */
	public void setSocket(DaemonSocketConnection socket) {
		//this.socket= socket;
	}

	/*
	 * @see IOldDaemonSupport#setInputParametersAsStrings(String[], int, int)
	 */
	public void setInputParametersAsStrings(String[] input, int key, int version) {
		//stringArray = input;
		//this.version =version;
		//this.key = key;
		
	}
	


}
