package org.eclipse.cdt.msw.debug.core.launch;

import org.eclipse.cdt.msw.debug.core.model.WinDebugTarget;
import org.eclipse.cdt.msw.debug.core.model.WinProcess;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

public class WinAppLaunchConfigDelegate implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if ("run".equals(mode))
			runLaunch(configuration, launch, monitor);
		else if ("debug".equals(mode))
			debugLaunch(configuration, launch, monitor);
	}

	private String getCommand(ILaunchConfiguration configuration) {
		// TODO
		return "notepad";
	}
	
	private String[] getArguments(ILaunchConfiguration configuration) {
		// TODO
		return null;
	}
	
	private void runLaunch(ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor) {
		String[] cmdline = new String[] { getCommand(configuration) };
		try {
			Process process = DebugPlugin.exec(cmdline, null);
			DebugPlugin.newProcess(launch, process, cmdline[0]);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void debugLaunch(ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor) {
		WinProcess process = new WinProcess(getCommand(configuration), getArguments(configuration), launch);
		launch.addProcess(process);
		
		WinDebugTarget target = new WinDebugTarget("Windows Debugger", launch, process);
		launch.addDebugTarget(target);
	}
	
	
}
