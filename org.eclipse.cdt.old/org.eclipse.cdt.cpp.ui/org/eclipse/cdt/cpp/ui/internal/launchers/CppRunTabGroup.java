package org.eclipse.cdt.cpp.ui.internal.launchers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class CppRunTabGroup extends AbstractLaunchConfigurationTabGroup {

	/**
	 * @see ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog, String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
      System.out.println("CppRunTabGroup:createTabs()");
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[2];
		tabs[0] = new CppRunInfoTab();
      tabs[1] = new CommonTab();
		setTabs(tabs);
	}

}
