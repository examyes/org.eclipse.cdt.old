package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/PICLLaunchWizard.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:00:22)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Iterator;
import java.util.Hashtable;

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.launch.PICLStartupInfo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.*;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This wizard is used when the debug or run button is pressed, and
 * the launcher/element resolution is not 1:1. It allows the user to
 * choose a launcher and element to launch.
 *
 * <p>The renderer used to render elements to launch is pluggable,
 * allowing launchers to provide custom renderers for the elements
 * they can launch.
 */

public class PICLLaunchWizard extends Wizard {

	//NLS
	private static final String PREFIX = "PICLLaunchWizard.";
	private static final String DEBUG = PREFIX + "debugTitle";

	/**
	 * The collection of available launchers
	 */
	protected Object[] fLaunchers;

	/**
	 * The resource providing context to determine launchables
	 */
	protected IStructuredSelection fSelection;

	/**
	 * The connection key associated with this launch.
	 */
	protected Object fConnectionKey;

	/**
	 * The startup info associated with this launch.
	 */
	protected PICLStartupInfo fStartupInfo;

	/**
	 * The property/value pairs from the debug engine.
	 */
	protected Hashtable fPairs;

	/**
	 * The launch page
	 */
	protected PICLLaunchWizardSelectionPage fPage;

	/**
	 * The old default launcher set for the <code>IProject</code>
	 * associated with the current selection.
	 */
	protected ILauncher fOldDefaultLauncher= null;

	/**
	 * Indicates if the default launcher has been set for the <code>IProject</code>
	 * associated with the current selection.
	 */
	 protected boolean fDefaultLauncherSet= false;

	/**
	 * Indicates if the wizard needs to determine the launcher to use
	 */
	 protected boolean fSelectLauncher;

	public PICLLaunchWizard(Object[] allLaunchers, IStructuredSelection selection, Object connectionKey, PICLStartupInfo startupInfo, Hashtable pairs) {
		this(allLaunchers, selection, connectionKey, startupInfo, pairs, true);
	}
	/**
	 * Constructs a wizard with a set of launchers, a selection, a mode
	 * and whether to select a launcher.
	 */
	public PICLLaunchWizard(Object[] allLaunchers, IStructuredSelection selection, Object connectionKey, PICLStartupInfo startupInfo, Hashtable pairs, boolean selectLauncher) {
		fSelectLauncher= selectLauncher;
		fLaunchers= allLaunchers;
		fSelection= selection;
		fConnectionKey = connectionKey;
		fStartupInfo = startupInfo;
		fPairs = pairs;
		initialize();
	}
	/**
	 * @see Wizard#addPages
	 */
	public void addPages() {
		if (fSelection == null || fSelection.isEmpty()) {
			addPage(new PICLLaunchWizardProjectSelectionPage());
		}
		if (fSelectLauncher) {
			addPage(fPage= new PICLLaunchWizardSelectionPage(fLaunchers));
		}
	}
	/**
	 * @see IWizard#canFinish()
	 */
	public boolean canFinish() {
		//it is the nested wizard that will finish
		return false;
	}
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
/*
		WorkbenchHelp.setHelp(
			pageContainer,
			new Object[] { IDebugHelpContextIds.LAUNCH_WIZARD });
*/
	}
	/**
	 * @see IWizard#getNextPage(IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		if (!fSelectLauncher) {
			IWizardNode node= new PICLLaunchWizardNode(page, (ILauncher)fLaunchers[0]);
			IWizard wizard = node.getWizard();
			wizard.addPages();
			return wizard.getStartingPage();
		}
		return super.getNextPage(page);
	}
	/**
	 * Returns the <code>IProject</code> that is associated with the context selection,
	 * or <code>null</code> if there is no single project associated with the selection.
	 */
	protected IProject getProject() {
		if (fSelection == null) {
			return null;
		}
		IProject project= null;
		Iterator elements= fSelection.iterator();
		while (elements.hasNext()) {
			Object e= elements.next();
			IResource res= null;
			if (e instanceof IAdaptable) {
				res= (IResource) ((IAdaptable) e).getAdapter(IResource.class);
				if (res == null) {
					res= (IResource) ((IAdaptable) e).getAdapter(IProject.class);
				}
			}
			if (res != null) {
				IProject p= res.getProject();
				if (project == null) {
					project= p;
				} else
					if (!project.equals(p)) {
						return null;
					}
			}
		}

		return project;
	}
	public Object getConnectionKey() {
		return fConnectionKey;
	}
	public PICLStartupInfo getStartupInfo() {
		if(fSelection != null)
			fStartupInfo.setResource(fSelection.getFirstElement());
		return fStartupInfo;
	}
	public Hashtable getPairs() {
		return fPairs;
	}
	protected void initialize() {
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(PICLUtils.getResourceString(DEBUG));
		setDefaultPageImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_LAUNCH_DEBUG));
	}
	/**
	 * @see IWizard#performCancel
	 */
	 public boolean performCancel() {
		PICLDebugPlugin.terminateEngine(fConnectionKey);
                fConnectionKey = null;

		if (fDefaultLauncherSet) {
			try {
				DebugPlugin.getDefault().getLaunchManager().setDefaultLauncher(getProject(), fOldDefaultLauncher);
			} catch (CoreException e) {
/*
				return false;
*/
			}
			fDefaultLauncherSet= false;
		}
		return true;
	}
	/**
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
		if (!fDefaultLauncherSet) {
			updateDefaultLauncher();
		}
		return true;
	}

	/**
	 * Sets the selection that is the context for the launch.
	 */
	public void setProjectSelection(IStructuredSelection selection) {
		fSelection= selection;
		if (fPage != null) {
			fPage.updateDefaultProject();
		}
	}
	/**
	 * Updates the default launcher if required - i.e. if the checkbox is
	 * checked.
	 */
	public void updateDefaultLauncher() {
		IProject project= getProject();
		if (fSelectLauncher && fPage.fSetAsDefaultLauncher.getSelection()) {
			ILauncher launcher= fPage.getLauncher();
			if (launcher != null) {
				try {
					fOldDefaultLauncher= DebugPlugin.getDefault().getLaunchManager().getDefaultLauncher(project);
					DebugPlugin.getDefault().getLaunchManager().setDefaultLauncher(project, launcher);
					fDefaultLauncherSet= true;
				} catch (CoreException e) {
				}
			}
		}
	}
}
