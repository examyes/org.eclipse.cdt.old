package com.ibm.cpp.ui.internal.launchers;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILauncherDelegate;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.debug.internal.picl.*;


public class CppLauncher implements ILauncherDelegate {

	private final String _defaultPort = "8001";
	
	/**bb
	 * @see ILauncherDelegate#launch(Object[], String, ILauncher)
	 */
	public boolean launch(Object[] elements, String mode, ILauncher launcher) {

    System.out.println("CppLauncher - launch()");
		// display the wizard
		StructuredSelection selection = new StructuredSelection(elements);
  /*
		PICLAttacherWizard w= new PICLAttacherWizard(defaultPort);
		w.init(launcher, ILaunchManager.DEBUG_MODE, selection);
		WizardDialog wd= new WizardDialog(PICLDebugPlugin.getActiveWorkbenchWindow().getShell(), w);
		return wd.open() == wd.OK;
  */

		Object element = null;
		if (selection != null)
         element = selection.getFirstElement();
		doLaunch(ILaunchManager.DEBUG_MODE, launcher, element);
      return true;
	}

	public boolean doLaunch(String mode, ILauncher launcher, Object selection) {


    System.out.println("CppLauncher - doLaunch()");
		IDebugTarget target= new PICLDebugUIDaemon(launcher, _defaultPort, selection);
		ILaunch launch = new Launch(launcher,mode, selection, null, null, target);
		DebugPlugin.getDefault().getLaunchManager().registerLaunch(launch);

   	ModelInterface api = ModelInterface.getInstance();
      String path = "e:/GdbPicl/test/";
      String invocation = "irmtdbgg +DJT_EVT=1 -qhost=localhost -quiport=8001 TestC.exe";
      api.command(path, invocation, false);

		return true;
	}

}
