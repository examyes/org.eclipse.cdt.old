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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;

import com.ibm.debug.daemon.CoreDaemon;
import com.ibm.debug.internal.pdt.PICLDebugTarget;
import com.ibm.debug.pdt.launch.PICLAttachInfo;

/**
 * Launches a debug attach session.
 */
public class CppDebugAttachLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

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
    	String processID = configuration.getAttribute(CppLaunchConfigConstants.ATTR_PROCESS_ID, "");
      IResource resource  = _api.findFile(executableName);
   	IProject project = resource.getProject();
	
	   _executable = _api.findResourceElement(resource);
   	DataElement projectElement = _api.getProjectFor(_executable);
      if (!project.isOpen())
      {
         displayMessageDialog(_plugin.getLocalizedString("runLauncher.Error.projectClosed"));
      	return;
		}

      PICLAttachInfo attachInfo = new PICLAttachInfo();
   	String qualifiedFileName = "";
	
   	qualifiedFileName = _executable.getSource();
   	IFile file = (IFile)_api.findFile(qualifiedFileName);
   	if (file == null)
      {
	   	projectElement = _api.getProjectFor(_executable);
   		project = _api.findProjectResource(projectElement);
   		file = new FileResourceElement(_executable, project);
	   	_api.addNewFile(file);			
      }
	
      attachInfo.setProcessID(processID);
      attachInfo.setProcessPath(qualifiedFileName);
      attachInfo.setStartupBehaviour(attachInfo.STOP);
		attachInfo.setLaunchConfig(configuration);

   	doLaunch(attachInfo, launch);
    }	

    public void doLaunch(PICLAttachInfo attachInfo, ILaunch launch)
    {
       CppSourceLocator sourceLocator = null;
	
	
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
	
	
  	   DataElement projectElement = _api.getProjectFor(_executable);
     	if (projectElement != null)
  	   {
		   sourceLocator = new CppSourceLocator(projectElement);
  			attachInfo.setWorkspaceSourceLocator(sourceLocator);
      }
  		_dataElementDirectory = _executable.getParent();
	
   	PICLDebugTarget target = new  PICLDebugTarget(attachInfo, attachInfo.getEngineConnectionInfo());
   	int key = CoreDaemon.generateKey();
   	CoreDaemon.storeDebugTarget(target, key);
        int port = CoreDaemon.getCurrentPort();
	
      launch.addDebugTarget(target);
   	launch.setSourceLocator(attachInfo.getWorkspaceSourceLocator());

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
	     MessageDialog.openError(CppPlugin.getActiveWorkbenchWindow().getShell(),_plugin.getLocalizedString("runLauncher.Error.Title"),message);
    }
	
			
}

