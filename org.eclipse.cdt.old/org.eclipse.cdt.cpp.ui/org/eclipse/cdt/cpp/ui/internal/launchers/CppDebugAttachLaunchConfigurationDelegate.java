package org.eclipse.cdt.cpp.ui.internal.launchers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IProject;
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

/**
 * Launches a debug attach session.
 */
public class CppDebugAttachLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException
   {
		System.out.println("CppDebugAttachLaunchConfigurationDelegate:launch() ");
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
			String id = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
			if (id == null) {
			//	IJavaProject javaProject = JavaLaunchConfigurationUtils.getJavaProject(configuration);
			//	ISourceLocator sourceLocator = new JavaSourceLocator(javaProject);
			//	launch.setSourceLocator(sourceLocator);
			}
		}
	}	
			
}

