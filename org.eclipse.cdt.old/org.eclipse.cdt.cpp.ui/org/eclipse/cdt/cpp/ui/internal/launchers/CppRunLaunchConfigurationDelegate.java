package org.eclipse.cdt.cpp.ui.internal.launchers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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

/**
 * Launches a local VM.
 */
public class CppRunLaunchConfigurationDelegate implements ILaunchConfigurationDelegate
 {

    private static DataElement _directory;
    private static DataElement _executable;
    private static ModelInterface _api;
    private static CppPlugin   _plugin;

	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException
   {
	   //System.out.println("CppRunLaunchConfigurationDelegate:launch() ");

      _plugin = CppPlugin.getDefault();
      _api = ModelInterface.getInstance();

		String executableName = config.getAttribute(CppLaunchConfigConstants.ATTR_EXECUTABLE_NAME, "");
      IResource file  = _api.findFile(executableName);
      DataElement _executable = _api.findResourceElement(file);
      DataElement projectElement = _api.getProjectFor(_executable);
      IProject project = _api.findProjectResource(projectElement);
      if (!project.isOpen())
      {
         displayMessageDialog(_plugin.getLocalizedString("runLauncher.Error.projectClosed"));
      	return;
		}
		//_directory = _executable.getParent();
    	String workingDirectory = config.getAttribute(CppLaunchConfigConstants.ATTR_WORKING_DIRECTORY, "");
//      String command = executableName + " " + parameters;
      String command = executableName;

	   //System.out.println("CppRunLaunchConfigurationDelegate:launch() command = " +command);
	   //System.out.println("CppRunLaunchConfigurationDelegate:launch() workingDirectory = " +workingDirectory);

     	_api.invoke(workingDirectory, command, false);


      /*
		String mainTypeName = verifyMainTypeName(configuration);

		IVMInstall vm = verifyVMInstall(configuration);

		IVMRunner runner = vm.getVMRunner(mode);
		if (runner == null) {
			abort(MessageFormat.format("Internal error: JRE {0} does not specify a VM Runner.", new String[]{vm.getId()}), null, IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST);
		}

		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null) {
			workingDirName = workingDir.getAbsolutePath();
		}
		
		// Program & VM args
		String pgmArgs = getProgramArguments(configuration);
		String vmArgs = getVMArguments(configuration);
		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
		
		// Classpath
		String[] classpath = getClasspath(configuration);
		
		// Create VM config
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);

		// Bootpath
		String[] bootpath = getBootpath(configuration);
		runConfig.setBootClassPath(bootpath);
		
		// Launch the configuration
		runner.run(runConfig, launch, monitor);		
		*/
		//  set default source locator if none specified
		if (launch.getSourceLocator() == null) {
			String id = config.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
			if (id == null) {
			//	IJavaProject javaProject = JavaLaunchConfigurationUtils.getJavaProject(configuration);
			//	ISourceLocator sourceLocator = new JavaSourceLocator(javaProject);
			//	launch.setSourceLocator(sourceLocator);
			}
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

