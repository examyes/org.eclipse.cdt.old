package org.eclipse.cdt.cpp.ui.internal.launchers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.ModelInterface;
import org.eclipse.cdt.dstore.core.model.DataElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Display;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ErrorDialog;

/**
 * Launches a program.
 */
public class CppRunLaunchConfigurationDelegate implements ILaunchConfigurationDelegate
 {

    private static ModelInterface _api;
    private static CppPlugin   _plugin;

     /**
      * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
      */
     public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException
     {
   	 _plugin = CppPlugin.getDefault();
   	 _api = ModelInterface.getInstance();
   	
   	 String executableName = config.getAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, "");
   	 IResource file  = _api.findFile(executableName);
       if (file == null)
       {
   		displayMessageDialog(_plugin.getLocalizedString("runLauncher.Error.invalidResource"));
   		return;
       }

   	 IProject project = file.getProject();
   	 if (!project.isOpen())
       {
   		 displayMessageDialog(_plugin.getLocalizedString("runLauncher.Error.projectClosed"));
   		 return;
       }
     	 String workingDirectory = config.getAttribute(CppLaunchConfigConstants.ATTR_WORKING_DIRECTORY, "");
     	 String parameters = config.getAttribute(CppLaunchConfigConstants.ATTR_PARAMETERS, "");
	    String command = executableName + " " + parameters;
	
     	 _api.invoke(workingDirectory, command, false);
	}	
		
   /**
     *	Display an error dialog with the specified message.
     *
     *	@param message java.lang.String
     */
    protected void displayMessageDialog(String message)
    {
		  IWorkbench workbench = CppPlugin.getDefault().getWorkbench();
        IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();

		  if (windows != null && windows.length > 0)
        {
				final Shell shell= windows[0].getShell();
            final String msg = message;

				if (!shell.isDisposed())
            {
              Display display = shell.getDisplay();

              display.asyncExec(new Runnable()
              {
                 public void run()
                 {
            	     MessageDialog.openError(shell, _plugin.getLocalizedString("runLauncher.Error.Title"), msg);
                 }
              });

				}
			}
    }
}

