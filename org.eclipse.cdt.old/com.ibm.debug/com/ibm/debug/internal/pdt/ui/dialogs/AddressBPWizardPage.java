package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/AddressBPWizardPage.java, eclipse, eclipse-dev, 20011128
// Version 1.11 (last modified 11/28/01 15:58:16)
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.DialogPageContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;

import com.ibm.debug.internal.pdt.ui.util.DialogField;
import com.ibm.debug.internal.pdt.ui.util.IDialogFieldListener;
import com.ibm.debug.internal.pdt.ui.util.SelectionButtonDialogField;
import com.ibm.debug.internal.pdt.ui.util.StringButtonDialogField;
import com.ibm.debug.internal.pdt.ui.util.StringDialogField;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLUtils;


/** The first page in the add address breakpoint wizard.*/
public class AddressBPWizardPage extends BreakpointWizardPage implements IDialogFieldListener, ISettingsWriter {

	private StringDialogField addressField;
	private SelectionButtonDialogField deferButton;

	private static final String TYPE = "AddressBPWizard";
	private static final String PAGE_NAME= "AddressBPWizard.page1";

	private static IDialogSettings section;
	private static final String ADDRESS ="Address"; //profile key
	private static final String PROJECT = "Project";  //profile key
	private static final String DEFER = "Defer";

	public static final String ADDRESS_BREAKPOINT_WIZARD_PAGE1 = "com.ibm.debug.add_address_breakpoint_action";

	//supports deferred breakpoints
	private boolean supportsDeferred = false;

	/**
	 * Constructor for AddressBPWizardPage
	 */
	protected AddressBPWizardPage(String pageName, String title, ImageDescriptor titleImage, boolean supportsDeferred) {
		super(pageName, title, titleImage);
		setDescription(PICLUtils.getResourceString(PAGE_NAME+".description"));
		this.supportsDeferred = supportsDeferred;
	}

	/**
	 * Constructor for AddressBPWizardPage
	 */
	protected AddressBPWizardPage(String pageName, String title, ImageDescriptor titleImage,boolean supportsDeferred, IMarker breakpoint) {
		super(pageName, title, titleImage, breakpoint);
		setDescription(PICLUtils.getResourceString(PAGE_NAME+".description"));
		this.supportsDeferred = supportsDeferred;
	}


	protected void createRequiredFields()
	{
		if(supportsDeferred)
		{
			deferButton = new SelectionButtonDialogField(SWT.CHECK|SWT.LEFT);
			deferButton.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".deferLabel"));
			deferButton.setDialogFieldListener(this);
		}

		projectField = new StringButtonDialogField(this);
		projectField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".projectLabel"));
		projectField.setDialogFieldListener(this);
		projectField.setButtonLabel(PICLUtils.getResourceString(PAGE_NAME+".browseLabel"));


		addressField = new StringDialogField();
		addressField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".addressLabel"));
		addressField.setDialogFieldListener(this);
	}


	/**
	 * This method initializes the dialog fields with the values of the existing
	 * breakpoint that the user is editing.
	 */
	private void initUsingOldBreakpoint()
	{

		try{		//todo: handle null responses
			projectField.setText(existingBP.getResource().getProject().getName()); //TODO
		 	projectField.setEnabled(false);
		 	addressField.setText((String)existingBP.getAttribute(IPICLDebugConstants.ADDRESS_EXPRESSION));
		 	if(!supportsDeferred)
		 		return;
		 	//defer attribute will be null if original BP set through dissassembly view
			if(existingBP.getAttribute(IPICLDebugConstants.DEFERRED)!= null)
				deferButton.setSelection( ((Boolean)(existingBP.getAttribute(IPICLDebugConstants.DEFERRED))).booleanValue() );
			else deferButton.setSelection(false);
			deferButton.setEnabled(false);

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

  		String text = section.get(ADDRESS);
  		if( text != null)
	  		addressField.setText(text);

	  	text = section.get(PROJECT);
	  	if( text != null)
	  		projectField.setText(text);
	  	else   //use name of current selected debug target's project
	  		projectField.setText(getNameOfCurrentSelectedProject());
	  	if(supportsDeferred)
		  	deferButton.setSelection(section.getBoolean(DEFER));
  	}

	/**
	 * @see WizardPage#createControl
	 */
	public void createControl(Composite parent) {

		super.createControl(parent);

		int nColumns= 3;
		if(supportsDeferred)
			deferButton.doFillIntoGrid(composite, nColumns);
		projectField.doFillIntoGrid(composite, nColumns);
		addressField.doFillIntoGrid(composite, nColumns);
		String pageHelpID = PICLUtils.getHelpResourceString("AddressBPWizardPage");
		//sets the help for any helpless widget on the page
		WorkbenchHelp.setHelp(getShell(), new DialogPageContextComputer(this, pageHelpID));
		//set widget specific help, with page help as backup
		WorkbenchHelp.setHelp(projectField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("AddressBPWizardPage.projectField") , pageHelpID });
		WorkbenchHelp.setHelp(projectField.getChangeControl(composite), new Object[] {PICLUtils.getHelpResourceString("AddressBPWizardPage.projectBrowse") , pageHelpID });
		WorkbenchHelp.setHelp(addressField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("AddressBPWizardPage.addressField") , pageHelpID});
		if(supportsDeferred)
			WorkbenchHelp.setHelp(deferButton.getSelectionButton(composite), new Object[] {PICLUtils.getHelpResourceString("AddressBPWizardPage.deferCheckBox") , pageHelpID });

		restoreSettings();
	}
	/**
	 * @see IDialogFieldListener#dialogFieldChanged
	 */
	public void dialogFieldChanged(DialogField field)
	{
		if(addressField.getText().equals("") || projectField.getText().equals("") )
			setPageComplete(false);
		else setPageComplete(true);
	}

	/**
	 * Returns the address or expression specified in the address field.
	 * For use by Wizard to set marker attributes.
	 */
	public String getAddress()
	{
		return addressField.getText();
	}

	/**
	 * Returns the state of the deferred check box.
	 * For use by Wizard to set marker attributes.
	 */
	public Boolean isDeferred()
	{
		if(supportsDeferred)
			return new Boolean(deferButton.isSelected());
		else return new Boolean(false);
	}

	/**
	 * @see ISettingsWriter#writeSettings
	 */
	public void writeSettings()
	{
		section.put(ADDRESS, addressField.getText());
		section.put(PROJECT, projectField.getText());
		if(supportsDeferred)
			section.put(DEFER, deferButton.isSelected());
		else section.put(DEFER, section.get(DEFER));
	}




}

