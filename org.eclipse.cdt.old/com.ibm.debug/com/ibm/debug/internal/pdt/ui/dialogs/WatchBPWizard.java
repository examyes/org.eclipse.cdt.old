package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/WatchBPWizard.java, eclipse, eclipse-dev, 20011129
// Version 1.14 (last modified 11/29/01 14:15:52)
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


public class WatchBPWizard extends BreakpointWizard{

	protected static final String DIALOG = "WatchBPWizard.dialog";
	protected static final String PAGE_1 = "WatchBPWizard.page1";
	protected static final String PAGE_2 = "BreakpointWizard.optional";

	boolean editing = false;
	IMarker existingBP;  //breakpoint user is editing


	/**
	 * Constructor for WatchBPWizard
	 */
	public WatchBPWizard() {
		super();
		initializeWizard();
	}

	public WatchBPWizard(IMarker breakpoint)
	{
		super();
		initializeWizard();
		editing = true;
		existingBP = breakpoint;
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
			wizardPage = new WatchBPWizardPage(PICLUtils.getResourceString(PAGE_1+".pageName"),
				PICLUtils.getResourceString(PAGE_1+".title"), null);
		else wizardPage = new WatchBPWizardPage(PICLUtils.getResourceString(PAGE_1+".pageName"),
				PICLUtils.getResourceString(PAGE_1+".title"), null, existingBP);
		addPage(wizardPage);

		findSelectedDebugTarget();


		//we'll create the page, even if we don't use it, to make sure we send all the same attributes
		//to the engine.  Otherwise, after editing, attributes from previous breakpoints may hang around.
		if(!editing)
			wizardPage = new ConditionalBreakpointWizardPage(PICLUtils.getResourceString(PAGE_2+".pageName"),
				PICLUtils.getResourceString(PAGE_2+".title"), null,false, frequencySupported(), threadsSupported());
		else wizardPage = new ConditionalBreakpointWizardPage(PICLUtils.getResourceString(PAGE_2+".pageName"),
				PICLUtils.getResourceString(PAGE_2+".title"), null,false, frequencySupported(), threadsSupported(), existingBP);
		if(conditionalBPSupported())
			addPage(wizardPage);

	}

   /**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {

		WatchBPWizardPage page = ((WatchBPWizardPage)getStartingPage());
		IStatus created = createMarker(page.getProjectResource(),
							page.getStartAddress(), page.getNumBytes());
		if(!created.isOK())
		{
			ErrorDialog.openError(getShell(),
				PICLUtils.getResourceString("ErrorDialog.error"),null , created);
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


	private IStatus createMarker(IResource resource, String startAddress, Integer numBytes)
	{
		StatusInfo status = new StatusInfo();

		if (resource == null)
		{
			status.setError(PICLUtils.getResourceString(PAGE_1+".resourceError"));
			return status;
		}
		if(startAddress == null || startAddress.equals(""))
		{
			status.setError(PICLUtils.getResourceString(PAGE_1+".addressError"));
			return status;
		}
		if(numBytes == null || numBytes.intValue() < 1)
		{
			status.setError(PICLUtils.getResourceString(PAGE_1+".bytesError"));
			return status;
		}

		try{
			IMarker breakpointMarker;
			if(!editing)
			{
				breakpointMarker = resource.createMarker(IPICLDebugConstants.PICL_WATCH_BREAKPOINT);
				DebugPlugin.getDefault().getBreakpointManager().configureBreakpoint(breakpointMarker,
					getPluginIdentifier(), true);
			}
			else breakpointMarker = existingBP;

			//     see the IPICLDebugConstants.PICL_WATCH_BREAKPOINT_ATTRIBUTES for an array of attribute names
			//     that are valid on a watch breakpoint.
			//     Use this array as the first parm to marker.setAttributes(String[] attributeNames, Object[] values)
			//     The second parm should be an array that matches the type expected by each of the attributes.
			//     See IPICLDebugConstants for the attribute types
			String[] attributeNames = {IPICLDebugConstants.UPDATE_BREAKPOINT, IPICLDebugConstants.EDITABLE,IPICLDebugConstants.ADDRESS_EXPRESSION,
				IPICLDebugConstants.NUM_BYTES_MONITORED,IPICLDebugConstants.THREAD, IPICLDebugConstants.TO_VALUE,
				IPICLDebugConstants.FROM_VALUE, IPICLDebugConstants.EVERY_VALUE, IPICLDebugConstants.ENGINE_ID_ATTRIBUTE};
			ConditionalBreakpointWizardPage conditionalPage = (ConditionalBreakpointWizardPage) getPage(PICLUtils.getResourceString(PAGE_2+".pageName"));
			Object[] values = {new Boolean(editing), new Boolean(true),  startAddress, numBytes, conditionalPage.getThreadValue(), conditionalPage.getToValue(),
				conditionalPage.getFromValue(), conditionalPage.getEveryValue(), target.getUniqueID()};
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


