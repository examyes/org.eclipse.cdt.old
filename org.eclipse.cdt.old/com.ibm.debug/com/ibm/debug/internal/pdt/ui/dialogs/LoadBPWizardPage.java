package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/LoadBPWizardPage.java, eclipse, eclipse-dev, 20011128
// Version 1.9 (last modified 11/28/01 15:58:22)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.DialogPageContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;

import com.ibm.debug.internal.pdt.ui.util.DialogField;
import com.ibm.debug.internal.pdt.ui.util.IDialogFieldListener;
import com.ibm.debug.internal.pdt.ui.util.StringButtonDialogField;
import com.ibm.debug.internal.pdt.ui.util.StringDialogField;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLUtils;

/** The first page in the add load breakpoint wizard.*/
public class LoadBPWizardPage extends BreakpointWizardPage implements IDialogFieldListener, ISettingsWriter {

	private StringDialogField dllField;

	private static final String PAGE_NAME= "LoadBPWizard.page1";
	private static IDialogSettings section;
	private static final String DLLNAME ="DLLName"; //profile key
	private static final String PROJECT = "Project";

	/**
	 * Constructor for LoadBPWizardPage
	 */
	protected LoadBPWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setDescription(PICLUtils.getResourceString(PAGE_NAME+".description"));
	}

	/**
	 * Constructor for LoadBPWizardPage when editing
	 */
	protected LoadBPWizardPage(String pageName, String title, ImageDescriptor titleImage, IMarker breakpoint) {
		super(pageName, title, titleImage, breakpoint);
		setDescription(PICLUtils.getResourceString(PAGE_NAME+".description"));
	}


	protected void createRequiredFields()
	{

		projectField = new StringButtonDialogField(this);
		projectField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".projectLabel"));
		projectField.setDialogFieldListener(this);
		projectField.setButtonLabel(PICLUtils.getResourceString(PAGE_NAME+".browseLabel"));

		dllField = new StringDialogField();
		dllField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".dllLabel"));
		dllField.setDialogFieldListener(this);
	}

	/**
	 * Returns the text in the DLL text field.
	 */
	public String getDLLName()
	{
		return dllField.getText();
	}

	/**
	 * @see WizardPage#createControl
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);

		int nColumns= 3;

		projectField.doFillIntoGrid(composite, nColumns);
		dllField.doFillIntoGrid(composite, nColumns);

		String pageHelpID = PICLUtils.getHelpResourceString("LoadBPWizardPage");
		//sets the help for any helpless widget on the page
		WorkbenchHelp.setHelp(getShell(), new DialogPageContextComputer(this, pageHelpID));
		//set widget specific help, with page help as backup
		WorkbenchHelp.setHelp(projectField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("LoadBPWizardPage.projectField") , pageHelpID });
		WorkbenchHelp.setHelp(projectField.getChangeControl(composite), new Object[] {PICLUtils.getHelpResourceString("LoadBPWizardPage.projectBrowse") , pageHelpID });
		WorkbenchHelp.setHelp(dllField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("LoadBPWizardPage.dllField") , pageHelpID});

		restoreSettings();
	}

	public void dialogFieldChanged(DialogField field)
	{
		if(dllField.getText().equals("") || projectField.getText().equals("") )
			setPageComplete(false);
		else setPageComplete(true);
	}

	/**
	 * This method initializes the dialog fields with the values of the existing
	 * breakpoint that the user is editing.
	 */
	private void initUsingOldBreakpoint()
	{

		try{		//todo: handle null responses
			dllField.setText((String)existingBP.getAttribute(IPICLDebugConstants.MODULE_NAME) );
			projectField.setText(existingBP.getResource().getProject().getName()); //TODO
		 	projectField.setEnabled(false);

	  	}catch(CoreException e){}
	}

	private void restoreSettings()
	{
		if(section == null)
		{
			IDialogSettings dialogSettings = getDialogSettings();
			if((section=dialogSettings.getSection(PAGE_NAME)) == null)
			{
				section=dialogSettings.addNewSection(PAGE_NAME);
			}
		}

		if(editing)
		{
			initUsingOldBreakpoint();
			return;
		}

  		String text = section.get(DLLNAME);
  		if( text != null)
	  		dllField.setText(text);

	  	text = section.get(PROJECT);
	  	if( text != null)
	  		projectField.setText(text);
	  	else   //use name of current selected debug target's project
	  		projectField.setText(getNameOfCurrentSelectedProject());
  	}


  	/**
	 * @see ISettingsWriter#writeSettings
	 */
	public void writeSettings()
	{
		section.put(DLLNAME, dllField.getText());
		section.put(PROJECT, projectField.getText());
	}

}

