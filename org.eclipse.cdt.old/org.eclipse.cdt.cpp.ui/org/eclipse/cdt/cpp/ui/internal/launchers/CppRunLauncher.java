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

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.wizards.*;


import java.io.IOException;


public class CppRunLauncher implements ILauncherDelegate
{
    private static DataElement _directory;
    private static DataElement _executable;
    private static ModelInterface _api;
    private static CppPlugin   _plugin;


    public CppRunLauncher()
    {
        _plugin = CppPlugin.getDefault();
	_api = ModelInterface.getInstance();
    }

public String getLaunchMemento(Object obj)
	{
		return null;	
	}
	
	public Object getLaunchObject(String mem)
	{
		return null;	
	}
    /**
     * @see ILauncherDelegate#launch(Object[], String, ILauncher)
     */
    public boolean launch(Object[] elements, String mode, ILauncher launcher)
    {
        IProject project;
        // Get the selection and check if valid
        StructuredSelection selection = new StructuredSelection(elements);
        if(selection == null)
        {
            displayMessageDialog(_plugin.getLocalizedString("runLauncher.Error.noSelection"));
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
         displayMessageDialog(_plugin.getLocalizedString("runLauncher.Error.notExecutable"));
			return false;
		    }
            DataElement projectElement = _api.getProjectFor(_executable);
            project = _api.findProjectResource(projectElement);
            if (!project.isOpen())
            {
               displayMessageDialog(_plugin.getLocalizedString("runLauncher.Error.projectClosed"));
			return false;
		    }
		_directory = _executable.getParent();
	    }	
        else if (element instanceof IProject || element instanceof IResource)
	    {
           project = ((IResource)element).getProject();
           if (!project.isOpen())
           {
              displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.projectClosed"));
              return false;
           }

		_executable = _api.findResourceElement((IResource)element);
		if (_executable == null)
		    {
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
      displayMessageDialog(_plugin.getLocalizedString("runLauncher.Error.notExecutable"));
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
        ModelInterface api = ModelInterface.getInstance();

        String command = program + " " + parameters;

        if (workingDirectory != "")
        {
         	_api.invoke(workingDirectory, command, false);
        }
        else
        {
            _api.invoke(_directory, command, false);
        }
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
