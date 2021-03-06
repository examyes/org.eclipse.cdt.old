package org.eclipse.cdt.cpp.ui.internal.launchers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.ModelInterface;
import org.eclipse.cdt.dstore.core.model.DataElement;
import org.eclipse.cdt.dstore.ui.resource.FileResourceElement;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;

import com.ibm.debug.daemon.CoreDaemon;
import com.ibm.debug.pdt.launch.PICLAttachInfo;
import com.ibm.debug.pdt.launch.PICLLaunchUtils;

/**
 * Launches a debug attach session.
 */
public class CppDebugAttachLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

    private CppPlugin                       _plugin;
    private ModelInterface                  _api;
    private DataElement                     _executable;
    private DataElement                     _dataElementDirectory;
    private IProject                        _project;

	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
  throws CoreException
   {
      String qualifiedFileName = "";
      _plugin = CppPlugin.getDefault();
      _api = ModelInterface.getInstance();

      String executableName = configuration.getAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, "");
      String processID = configuration.getAttribute(CppLaunchConfigConstants.ATTR_PROCESS_ID, "");
      IResource resource  = _api.findFile(executableName);

      if (resource != null)
      {
      	 _project = resource.getProject();
   	 _executable = _api.findResourceElement(resource);
      	
         if (!_project.isOpen())
         {
            displayMessageDialog(_plugin.getLocalizedString("runLauncher.Error.projectClosed"));
            return;
	 }

      	 if (_executable != null)
             qualifiedFileName = _executable.getSource();
         else
             qualifiedFileName = resource.getLocation().toString();

      	 IFile file = (IFile)_api.findFile(qualifiedFileName);
      	 if (file == null)
         {
   	   	DataElement projectElement = _api.getProjectFor(_executable);
      		_project = _api.findProjectResource(projectElement);
      		file = new FileResourceElement(_executable, _project);
   	   	_api.addNewFile(file);			
         }
      }

      PICLAttachInfo attachInfo = new PICLAttachInfo();
	
      attachInfo.setProcessID(processID);
      attachInfo.setProcessPath(qualifiedFileName);
      attachInfo.setStartupBehaviour(attachInfo.STOP);
		attachInfo.setLaunchConfig(configuration);

   	doLaunch(attachInfo, launch);
    }	

    public void doLaunch(PICLAttachInfo attachInfo, ILaunch launch)
    {
        //ensure the daemon is listening
	//        int port = DaemonLauncherDelegate.launchDaemon(_elements);
       DataElement  projectElement;
       boolean ok = CoreDaemon.startListening();
       if (ok == true)
       {
          int port = CoreDaemon.getCurrentPort();
	  if (port < 0)
          {
             return;
          }
       }
       else
       {
	  return;
       }
	
	
       if (_executable != null)
       {
          _dataElementDirectory = _executable.getParent();
       }
       else
       {
          _dataElementDirectory = null;
       }

       IDebugTarget target = PICLLaunchUtils.getDebugTarget(launch, attachInfo);
       int key = CoreDaemon.generateKey();
       CoreDaemon.storeDebugTarget(target, key);
        int port = CoreDaemon.getCurrentPort();
	
        launch.addDebugTarget(target);
	
   	// locator
        if (_executable != null)
            projectElement = _api.getProjectFor(_executable);
        else
            projectElement = _api.findResourceElement(_project);

   	if (projectElement != null)
	    {
   		// source locator
	   	CppSourceLocator sourceLocator = new CppSourceLocator(projectElement);
	   	launch.setSourceLocator(sourceLocator);
	    }
   	else
	    {
	   	launch.setSourceLocator(attachInfo.getWorkspaceSourceLocator());
	    }
	
   	launchEngine(new Integer(port).toString(), new Integer(key).toString());
    }

    protected void launchEngine(String port, String key)
    {
     	_api.debug(_dataElementDirectory, port, key);
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
            	     MessageDialog.openError(shell, _plugin.getLocalizedString("attachLauncher.Error.Title"), msg);
                 }
              });

				}
			}
    }
}

