package org.eclipse.cdt.cpp.ui.internal.launchers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.resource.*;
import com.ibm.debug.daemon.CoreDaemon;
import com.ibm.debug.launch.PICLDaemonInfo;
import com.ibm.debug.launch.PICLLoadInfo;
import com.ibm.debug.internal.picl.PICLDebugTarget;

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
	System.out.println("CppDebugLoadLaunchConfigurationDelegate:launch() ");
	_plugin = CppPlugin.getDefault();
	_api = ModelInterface.getInstance();

	String executableName = configuration.getAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, "");
	IResource resource  = _api.findFile(executableName);
	IProject project = resource.getProject();
	
	_executable = _api.findResourceElement(resource);
	DataElement projectElement = _api.getProjectFor(_executable);
	if (!project.isOpen())
	    {
		displayMessageDialog(_plugin.getLocalizedString("runLauncher.Error.projectClosed"));
		return;
	    }
	String workingDirectory = configuration.getAttribute(CppLaunchConfigConstants.ATTR_WORKING_DIRECTORY, "");
	//  set default source locator if none specified
	if (launch.getSourceLocator() == null) {
	    String id = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
	    if (id == null) {
		//	IJavaProject javaProject = JavaLaunchConfigurationUtils.getJavaProject(configuration);
		//	ISourceLocator sourceLocator = new JavaSourceLocator(javaProject);
		//	launch.setSourceLocator(sourceLocator);
	    }
	}

   	PICLLoadInfo loadInfo = new PICLLoadInfo();
	String qualifiedFileName = "";
	
	qualifiedFileName = _executable.getSource();
	System.out.println("CppDebugLoadLaunchConfigurationDelegate:launch() - qualifiedFileName = " + qualifiedFileName);
	IFile file = (IFile)_api.findFile(qualifiedFileName);
   	if (file == null)
	    {
	   	projectElement = _api.getProjectFor(_executable);
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
	
	
	doLaunch(loadInfo, workingDirectory);
	
    }	
    
    public void doLaunch(PICLLoadInfo loadInfo, String workingDirectory)
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
	
	
	if (_executable != null)
	    {
		DataElement projectElement = _api.getProjectFor(_executable);
		if (projectElement != null)
		    {
			sourceLocator = new CppSourceLocator(projectElement);
                    System.out.println("CppDebugLoadLaunchConfigurationDelegate:doLaunch() calling loadInfo.setWorkspaceSourceLocator() ");
			loadInfo.setWorkspaceSourceLocator(sourceLocator);
		    }
		_dataElementDirectory = _executable.getParent();
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
	     MessageDialog.openError(CppPlugin.getActiveWorkbenchWindow().getShell(),_plugin.getLocalizedString("runLauncher.Error.Title"),message);
    }
	
			
}

