/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.rpm.ui;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;


/**
 * @author pmuldoon
 * @version 1.0
 *
 *
 * Plug-in entry point. When the user chooses to export an SRPM ,the plug-in manager in Eclipse
 * will invoke this class. This class extends Wizard and implements IExportWizard.
 *
 * The main plugin class to be used in the desktop. This is the "entrypoint"
 * for the export rpm plug-in.
 */
public class RPMExportWizard extends Wizard implements IExportWizard {
	// Create a local reference to RPMExportPage
	RPMExportPage mainPage;
	RPMExportPage_2 patchPage;

	/**
	 * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 *
	 * Basic constructor. Don't do much, just print out debug, and set progress
	 * monitor status to true
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.getString("RPMExportWizard.Export_an_SRPM")); //$NON-NLS-1$
	}

	// We have elected to do the Finish button clickin in the RPMExportPage. So override
	//the default and point to RPMExport finish()
	public boolean performFinish() {
		try {
			return mainPage.finish(patchPage.patchData());
		} catch (CoreException e) {
			return false;
		}
	}

	public boolean canFinish() {
		if (!mainPage.canGoNext()) {
			return mainPage.canFinish();
		} else if (mainPage.canFinish() && patchPage.canFinish()) {
			return true;
		}

		return false;
	}

	// Add the RPMExportPage as the only page in this wizard.
	public void addPages() {
		mainPage = new RPMExportPage();
		addPage(mainPage);
		patchPage = new RPMExportPage_2();
		addPage(patchPage);
	}
}
