package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/LoadBPWizard.java, eclipse, eclipse-dev, 20011129
// Version 1.14 (last modified 11/29/01 14:15:51)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.pdt.ui.util.StatusInfo;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLUtils;


public class LoadBPWizard extends BreakpointWizard {

	protected static final String DIALOG = "LoadBPWizard.dialog";
	protected static final String PAGE_1 = "LoadBPWizard.page1";
	protected static final String PAGE_2 = "BreakpointWizard.optional";

	IMarker existingBP;
	boolean editing = false;

	/**
	 * Constructor for LoadBPWizard
	 */
	public LoadBPWizard() {
		super();
		initializeWizard();
	}

	public LoadBPWizard(IMarker breakpoint)
	{
		super();
		initializeWizard();
		editing = true;
		existingBP=breakpoint;
	}

	private void initializeWizard()
	{
		setWindowTitle(PICLUtils.getResourceString(DIALOG+".title"));
		setDefaultPageImageDescriptor(PICLUtils.getImageDescriptor(IPICLDebugConstants.PICL_ICON_BREAKPOINT_WIZARD));

		setNeedsProgressMonitor(false);

		//get the default picl settings file
		IDialogSettings dialogSettings = PICLDebugPlugin.getDefault().getDialogSettings();

		IDialogSettings section = dialogSettings.getSection(DIALOG);
		if(section==null)
			//create a subsection for use by pages of this wizard
			section= dialogSettings.addNewSection(DIALOG);

		//set which file/subsection the wizard pages should use for storing their settings
		setDialogSettings(section);
	}

	/**
	 * @see Wizard#createPages
	 */
	public void addPages()
	{
		super.addPages();
		WizardPage wizardPage;
		if(!editing)
			wizardPage = new LoadBPWizardPage(PICLUtils.getResourceString(PAGE_1+".pageName"),
				PICLUtils.getResourceString(PAGE_1+".title"), null);
		else wizardPage = new LoadBPWizardPage(PICLUtils.getResourceString(PAGE_1+".pageName"),
				PICLUtils.getResourceString(PAGE_1+".title"), null, existingBP);
		addPage(wizardPage);

		findSelectedDebugTarget();


		//we'll create the page, even if we don't use it, to make sure we send all the same attributes
		//to the engine.  Otherwise, after editing, attributes from previous breakpoints may hang around.
		if(!editing)
			wizardPage = new ConditionalBreakpointWizardPage(PICLUtils.getResourceString(PAGE_2+".pageName"),
				PICLUtils.getResourceString(PAGE_2+".title"), null,false, false, threadsSupported());
		else wizardPage = new ConditionalBreakpointWizardPage(PICLUtils.getResourceString(PAGE_2+".pageName"),
				PICLUtils.getResourceString(PAGE_2+".title"), null,false, false, threadsSupported(), existingBP);
		if(conditionalBPSupported())
			addPage(wizardPage);

	}

   /**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		IStatus created = createMarker(	((LoadBPWizardPage)getStartingPage()).getProjectResource(),
				((LoadBPWizardPage)getStartingPage()).getDLLName());
		if(!created.isOK())
		{
			ErrorDialog.openError(getShell(),
				PICLUtils.getResourceString("ErrorDialog.error"), null , created);
			return false;
		}
		else
		{
			// tell all pages to save current settings to file
			IWizardPage[] pages = getPages();
			for(int i=0; i < getPageCount(); i++)
			{
				if(pages[i] instanceof ISettingsWriter)
					((ISettingsWriter)pages[i]).writeSettings();
			}
			//write file to disk
			PICLDebugPlugin.getDefault().saveDialogSettings();
		}
		return true;
	}


	private IStatus createMarker(IResource resource, String dllName)
	{
		StatusInfo status = new StatusInfo();

		if (resource == null)
		{
			status.setError(PICLUtils.getResourceString(PAGE_1+".resourceError"));
			return status;
		}
		if(dllName == null || dllName.equals(""))
		{
			status.setError(PICLUtils.getResourceString(PAGE_1+".dllError"));
			return status;
		}
		try{
			IMarker breakpointMarker = null;
			if(!editing)
			{
				breakpointMarker = resource.createMarker(IPICLDebugConstants.PICL_LOAD_BREAKPOINT);
				DebugPlugin.getDefault().getBreakpointManager().configureBreakpoint(breakpointMarker,
					getPluginIdentifier(), true);
			}
			else
				breakpointMarker = existingBP;

			//     see the IPICLDebugConstants.PICL_LOAD_BREAKPOINT_ATTRIBUTES for an array of attribute names
			//     that are valid on a load breakpoint.
			//     Use this array as the first parm to marker.setAttributes(String[] attributeNames, Object[] values)
			//     The second parm should be an array that matches the type expected by each of the attributes.
			//     See IPICLDebugConstants for the attribute types

			String[] attributeNames = {IPICLDebugConstants.UPDATE_BREAKPOINT, IPICLDebugConstants.EDITABLE,IPICLDebugConstants.MODULE_NAME,
				IPICLDebugConstants.THREAD, IPICLDebugConstants.ENGINE_ID_ATTRIBUTE};
			ConditionalBreakpointWizardPage conditionalPage = (ConditionalBreakpointWizardPage) getPage(PICLUtils.getResourceString(PAGE_2+".pageName"));
			Object[] values = {new Boolean(editing), new Boolean(true), dllName, conditionalPage.getThreadValue(),target.getUniqueID()};
			breakpointMarker.setAttributes(attributeNames, values);


			if(!editing)
				DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(breakpointMarker);
			if(breakpointMarker.getAttribute(IPICLDebugConstants.ERROR) !=null)
			{
				status.setError((String)breakpointMarker.getAttribute(IPICLDebugConstants.ERROR_MSGTEXT));
				//TODO: highlight IPICLDebugConstant.ERROR_ATTRIBUTE
				//clear attributes
				breakpointMarker.setAttribute(IPICLDebugConstants.ERROR_MSGTEXT, null);
				breakpointMarker.setAttribute(IPICLDebugConstants.ERROR_ATTRIBUTE, null);
				if(!editing)
					DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpointMarker, true);
			}
			return status;

		}
		catch (CoreException e) {
			return e.getStatus();
		}
	}

}

