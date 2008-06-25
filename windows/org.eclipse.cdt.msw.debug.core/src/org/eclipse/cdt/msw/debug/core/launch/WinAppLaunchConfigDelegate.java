package org.eclipse.cdt.msw.debug.core.launch;

import org.eclipse.cdt.msw.debug.core.controller.WinDebugController;
import org.eclipse.cdt.msw.debug.core.model.WinDebugEventCallbacks;
import org.eclipse.cdt.msw.debug.dbgeng.DebugCreateProcessOptions;
import org.eclipse.cdt.msw.debug.dbgeng.HRESULTException;
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
		StringBuffer cmdLineBuff = new StringBuffer(getCommand(configuration));
		String[] args = getArguments(configuration);
		if (args != null)
			for (String arg : args) {
				cmdLineBuff.append(' ');
				cmdLineBuff.append(arg);
			}
		
		final String cmdLine = cmdLineBuff.toString();
		final WinDebugController controller = WinDebugController.getController();
		try {
			controller.getDebugClient().setEventCallbacks(new WinDebugEventCallbacks(launch));
		} catch (HRESULTException e1) {
			e1.printStackTrace();
		}
		
		controller.enqueueCommand(new Runnable() {
			@Override
			public void run() {
				try {
					controller.getDebugClient().createProcess(0, cmdLine,
							DebugCreateProcessOptions.DEBUG_PROCESS);
					controller.go(true);
				} catch (HRESULTException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
}
