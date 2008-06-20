package org.eclipse.cdt.msw.debug.ui.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

public class WinLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {

	public WinLaunchConfigurationTabGroup() {
	}

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new WinMainTab(),
				new EnvironmentTab(),
				new SourceLookupTab(),
				new CommonTab() 
			};
		setTabs(tabs);
	}

}
