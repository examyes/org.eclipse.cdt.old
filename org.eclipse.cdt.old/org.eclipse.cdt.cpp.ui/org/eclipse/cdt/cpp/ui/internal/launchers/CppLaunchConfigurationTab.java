package org.eclipse.cdt.cpp.ui.internal.launchers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
//import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;


import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.*;

/**
 * Common function for C/C++ launch configuration tabs.
 */
public abstract class CppLaunchConfigurationTab extends AbstractLaunchConfigurationTab {
		
	/**
	 * Returns the current selection from which to initialize
	 * default settings, or <code>null</code> if none.
	 *
	 * @return Object.
	 */

	protected IStructuredSelection getSelection()
   {

      System.out.println("CppLaunchConfigurationTab:getSelection");
		IWorkbenchWindow window= CppPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			ISelection selection= window.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				return (IStructuredSelection) selection;
			}
		}
		return null;
	}
	
	/**
	 * Set the java project attribute based on the IJavaElement.
	 */
/*
	protected void initializeJavaProject(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		IJavaProject javaProject = javaElement.getJavaProject();
		String name = null;
		if (javaProject != null && javaProject.exists()) {
			name = javaProject.getElementName();
		}
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
	}	
*/
}