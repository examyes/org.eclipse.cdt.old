package org.eclipse.cdt.msw.debug.core.launch;

import org.eclipse.cdt.msw.debug.core.model.WinProcess;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;

public class WinAppLaunchConfigDelegate implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IProcess process = new WinProcess("notepad", null, launch);
		launch.addProcess(process);
	}

}
