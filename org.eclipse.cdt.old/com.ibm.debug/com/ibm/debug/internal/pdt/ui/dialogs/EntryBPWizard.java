package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/EntryBPWizard.java, eclipse, eclipse-dev, 20011129
// Version 1.15 (last modified 11/29/01 14:15:49)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */



import java.util.Vector;

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


public class EntryBPWizard extends BreakpointWizard {

	protected static final String DIALOG = "EntryBPWizard.dialog";
	protected static final String PAGE_1 = "EntryBPWizard.page1";
	protected static final String PAGE_2 = "BreakpointWizard.optional";

	IMarker existingBP;
	boolean editing = false;

	/**
	 * Constructor for EntryBPWizard
	 */
	public EntryBPWizard() {
		super();
		initializeWizard();
	}

	public EntryBPWizard(IMarker breakpoint){
		super();
		initializeWizard();
		existingBP=breakpoint;
		editing = true;
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
			wizardPage = new EntryBPWizardPage(PICLUtils.getResourceString(PAGE_1+".pageName"),
				PICLUtils.getResourceString(PAGE_1+".title"), null, deferredSupported());
		else wizardPage = new EntryBPWizardPage(PICLUtils.getResourceString(PAGE_1+".pageName"),
				PICLUtils.getResourceString(PAGE_1+".title"), null, deferredSupported(), existingBP);
		addPage(wizardPage);

		findSelectedDebugTarget();

		//we'll create the page, even if we don't use it, to make sure we send all the same attributes
		//to the engine.  Otherwise, after editing, attributes from previous breakpoints may hang around.
		if(!editing)
			wizardPage = new ConditionalBreakpointWizardPage(PICLUtils.getResourceString(PAGE_2+".pageName"),
				PICLUtils.getResourceString(PAGE_2+".title"), null,expressionsSupported(), frequencySupported(),
				threadsSupported());
		else wizardPage = new ConditionalBreakpointWizardPage(PICLUtils.getResourceString(PAGE_2+".pageName"),
				PICLUtils.getResourceString(PAGE_2+".title"), null,expressionsSupported(), frequencySupported(),
				threadsSupported(), existingBP);
		if(conditionalBPSupported())
			addPage(wizardPage);

	}



	/**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish()
	{

		EntryBPWizardPage page = (EntryBPWizardPage)getStartingPage();
		IResource resource = page.getSourceResource();
		if(resource == null) //may be null if source is not in workbench
			resource = page.getProjectResource();
		IStatus created = createMarker(resource,  page.getSourceName(),
			page.getEntryName(), page.isDeferred(), page.isCaseSensitive());
		if(!created.isOK())
		{
			ErrorDialog.openError(getShell(), PICLUtils.getResourceString("ErrorDialog.error"),null , created);
			return false;
		}

		// tell all pages to save current settings to file
		IWizardPage[] pages = getPages();
		for(int i=0; i < getPageCount(); i++)
		{
			if(pages[i] instanceof ISettingsWriter)
				((ISettingsWriter)pages[i]).writeSettings();
		}
		//write file to disk
		PICLDebugPlugin.getDefault().saveDialogSettings();

		return true;
	}





	private IStatus createMarker(IResource resource, String sourceName, String entryName, Boolean defer, Boolean caseSensitive)
	{
		StatusInfo status = new StatusInfo();

		if (resource == null)
		{
			status.setError(PICLUtils.getResourceString(PAGE_1+".resourceError"));
			return status;
		}
		if(entryName == null || entryName.equals(""))
		{
			status.setError(PICLUtils.getResourceString(PAGE_1+".entryError"));
			return status;
		}
	/*	Vector overloaded = getOverloadPossibilities(entryName, sourceName, resource, caseSensitive);
		if(overloaded.size() > 1)
		{
			OverloadedDialog overloadDialog = new OverloadedDialog(getShell());//, overloaded);
			overloadDialog.open();
			String methodName = overloadDialog.getChoice();
		}*/
		try{
			IMarker breakpointMarker = null;
			if(!editing)
			{
				breakpointMarker = resource.createMarker(IPICLDebugConstants.PICL_ENTRY_BREAKPOINT);
				DebugPlugin.getDefault().getBreakpointManager().configureBreakpoint(breakpointMarker, getPluginIdentifier(), true);
			}
			else breakpointMarker = existingBP;

			//     see the IPICLDebugConstants.PICL_ENTRY_BREAKPOINT_ATTRIBUTES for an array of attribute names
			//     that are valid on an entry breakpoint.
			//     Use this array as the first parm to marker.setAttributes(String[] attributeNames, Object[] values)
			//     The second parm should be an array that matches the type expected by each of the attributes.
			//     See IPICLDebugConstants for the attribute types
			String[] attributeNames = {IPICLDebugConstants.UPDATE_BREAKPOINT,IPICLDebugConstants.SOURCE_FILE_NAME,
				IPICLDebugConstants.EDITABLE,IPICLDebugConstants.DEFERRED,IPICLDebugConstants.THREAD,
				IPICLDebugConstants.EVERY_VALUE,IPICLDebugConstants.TO_VALUE,IPICLDebugConstants.FROM_VALUE,
				IPICLDebugConstants.CONDITIONAL_EXPRESSION,IPICLDebugConstants.FUNCTION_NAME, IPICLDebugConstants.CASESENSITIVE,
				IPICLDebugConstants.ENGINE_ID_ATTRIBUTE};
			ConditionalBreakpointWizardPage conditionalPage = (ConditionalBreakpointWizardPage) getPage(PICLUtils.getResourceString(PAGE_2+".pageName"));
			Object[] values = {new Boolean(editing), sourceName, new Boolean(true), defer, conditionalPage.getThreadValue(), conditionalPage.getEveryValue(),
				conditionalPage.getToValue(), conditionalPage.getFromValue(),conditionalPage.getExpression(),
				entryName, caseSensitive, target.getUniqueID()};
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



	private Vector getOverloadPossibilities(String entryName, String sourceName, IResource projectResource, Boolean caseSensitive)
	{
		Vector possibilities=null;

		return possibilities;
	}


}


