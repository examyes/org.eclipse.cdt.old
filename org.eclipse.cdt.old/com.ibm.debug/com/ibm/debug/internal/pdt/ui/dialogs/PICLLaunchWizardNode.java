package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/PICLLaunchWizardNode.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:00:20)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Hashtable;

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.launch.IPICLLaunchWizard;
import com.ibm.debug.launch.PICLStartupInfo;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.ILaunchWizard;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Point;

/**
 * A wizard node represents a "potential" wizard. Wizard nodes
 * allow the user to pick from several available nested wizards.
 */
public class PICLLaunchWizardNode implements IWizardNode {

	private static final String PREFIX= "PICLLaunchWizardNode.";
	private static final String ERROR= PREFIX + "error";

	protected IWizard fWizard;
	protected IWizardPage fParentWizardPage;
	protected ILauncher fLauncher;

	/**
	 * Creates a node that holds onto a wizard element.
	 * The wizard element provides information on how to create
	 * the wizard supplied by the ISV's extension.
	 */
	public PICLLaunchWizardNode(IWizardPage aWizardPage, ILauncher launcher) {
		fParentWizardPage= aWizardPage;
		fLauncher= launcher;
	}
	/**
	 * Returns the wizard represented by this wizard node.
	 */
	public ILaunchWizard createWizard() throws CoreException {
		IConfigurationElement config= fLauncher.getConfigurationElement();
                PICLLaunchWizard wizard = (PICLLaunchWizard)fParentWizardPage.getWizard();
                PICLStartupInfo startupInfo = wizard.getStartupInfo();
		startupInfo.setLauncher(fLauncher);
		ILaunchWizard launchWizard= (ILaunchWizard)PICLDebugPlugin.getDefault().createExtension(config, "wizard");
		((IPICLLaunchWizard)launchWizard).init(wizard.getConnectionKey(), startupInfo, wizard.getPairs());
		return launchWizard;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizardNode#dispose()
	 */
	public void dispose() {
		// Do nothing since the wizard wasn't created via reflection.
		fWizard= null;
	}
	/**
	 * Returns the description specified for the launcher associated
	 * with the wizard node.
	 */
	public String getDescription() {
		IConfigurationElement config= fLauncher.getConfigurationElement();
		String description= config.getAttribute("description");
		if (description == null) {
			description= "";
		}
		return description;
	}
	/**
	 * @see org.eclipse.jface.wizard.IWizardNode#getExtent()
	 */
	public Point getExtent() {
		return new Point(-1, -1);
	}
	/**
	 * @see org.eclipse.jface.wizards.IWizardNode#getWizard()
	 */
	public IWizard getWizard() {
		if (fWizard != null) {
			return fWizard; // we've already created it
		}
		try {
			fWizard= createWizard(); // create instance of target wizard
		} catch (CoreException e) {
			ErrorDialog.openError(fParentWizardPage.getControl().getShell(), PICLUtils.getResourceString("ErrorDialog.title"), PICLUtils.getResourceString(ERROR), e.getStatus());
			return null;
		}

		return fWizard;
	}
	/**
	 * @see org.eclipse.jface.wizard.IWizardNode#isContentCreated()
	 */
	public boolean isContentCreated() {
		return fWizard != null;
	}
}
