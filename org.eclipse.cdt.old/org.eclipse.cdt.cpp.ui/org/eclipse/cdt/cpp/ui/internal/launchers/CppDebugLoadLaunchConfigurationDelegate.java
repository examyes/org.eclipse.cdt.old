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
import com.ibm.debug.pdt.launch.PICLLaunchUtils;
import com.ibm.debug.pdt.launch.PICLLoadInfo;

/**
 * Launches a local VM.
 */
public class CppDebugLoadLaunchConfigurationDelegate implements ILaunchConfigurationDelegate
{

    private CppPlugin                       _plugin;
    private ModelInterface                  _api;
    private DataElement                     _executable;
    private DataElement                     _dataElementDirectory;

	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
	 */
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
	throws CoreException
    {
	_plugin = CppPlugin.getDefault();
	_api = ModelInterface.getInstance();

	String executableName = configuration.getAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, "");
	IResource resource  = _api.findFile(executableName);
   if (resource == null)
    {
		displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.invalidResource"));
		return;
    }

	IProject project = resource.getProject();
	
	_executable = _api.findResourceElement(resource);

	if (!project.isOpen())
    {
		displayMessageDialog(_plugin.getLocalizedString("loadLauncher.Error.projectClosed"));
		return;
    }
	String workingDirectory = configuration.getAttribute(CppLaunchConfigConstants.ATTR_WORKING_DIRECTORY, "");

	//  set default source locator if none specified
	if (launch.getSourceLocator() == null)
    {
		String id = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
		if (id == null)
	    {
	    }
    }

   	PICLLoadInfo loadInfo = new PICLLoadInfo();
	String qualifiedFileName = "";
	
	qualifiedFileName = _executable.getSource();
	IFile file = (IFile)_api.findFile(qualifiedFileName);
   	if (file == null)
	    {
      	DataElement projectElement = _api.getProjectFor(_executable);
		   project = _api.findProjectResource(projectElement);
   		file = new FileResourceElement(_executable, project);
	   	_api.addNewFile(file);			
	    }
	
	//  loadInfo.setResource(file);  RW  gone??
	
   	//loadInfo.setLauncher(_launcher);
   	loadInfo.setProgramName(qualifiedFileName);
   	//loadInfo.setProgramParms(parameters);  RW - todo
	
	int startupBehaviour;
	
	//	if (debugInitialization)
	//	    {
	//		startupBehaviour = loadInfo.DEBUG_INITIALIZATION;
	//	    }
	//	else
	//	    {
	startupBehaviour = loadInfo.RUN_TO_MAIN;
	//	    }
	
	loadInfo.setStartupBehaviour(startupBehaviour);
	
	
	doLaunch(loadInfo, launch, workingDirectory);
	
    }	

    public void doLaunch(PICLLoadInfo loadInfo, ILaunch launch, String workingDirectory)
    {
	
	
        //ensure the daemon is listening
	//        int port = DaemonLauncherDelegate.launchDaemon(_elements);
        boolean ok = CoreDaemon.startListening();
        if (ok == true)
	    {
		int port = CoreDaemon.getCurrentPort();
		if (port < 0)
		    return;
	    }
        else
	    return;
	
	
	if (_executable != null)
	    {
		_dataElementDirectory = _executable.getParent();
	    }
	
	IDebugTarget target = PICLLaunchUtils.getDebugTarget(launch, loadInfo);
	int key = CoreDaemon.generateKey();
	CoreDaemon.storeDebugTarget(target, key);
        int port = CoreDaemon.getCurrentPort();
		
        launch.addDebugTarget(target);

	// locator
	DataElement projectElement = _api.getProjectFor(_executable);
	if (projectElement != null)
	    {
		// source locator
		CppSourceLocator sourceLocator = new CppSourceLocator(projectElement);
		launch.setSourceLocator(sourceLocator);
	    }
	else
	    {
		launch.setSourceLocator(loadInfo.getWorkspaceSourceLocator());
	    }

	//target.launchEngine(key);    		

	launchEngine(workingDirectory, new Integer(port).toString(), new Integer(key).toString());
    }

    protected void launchEngine(String workingDirectory, String port, String key)
    {
      if (workingDirectory != "")
      {
         IResource resource = _api.getResource(workingDirectory);
         DataElement deDirectory = _api.findResourceElement(resource);

       	_api.debug(deDirectory, port, key);
      }
      else
      {
       	_api.debug(_dataElementDirectory, port, key);
      }
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
            	     MessageDialog.openError(shell, _plugin.getLocalizedString("loadLauncher.Error.Title"), msg);
                 }
              });

				}
			}
    }
}

