package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/WatchBPWizardPage.java, eclipse, eclipse-dev, 20011128
// Version 1.10 (last modified 11/28/01 15:58:24)
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


/**
 * Subclasses may override these methods if required:
 * <ul>
 *  <li><code>performHelp</code> - may be reimplemented to display help for the page</li>
 *  <li><code>dispose</code> - may be extended to dispose additional allocated SWT resources</li>
 * </ul>
 * </p>
 */

/** The first page in the add watch breakpoint wizard.*/
public class WatchBPWizardPage extends BreakpointWizardPage implements IDialogFieldListener, ISettingsWriter {

	private StringDialogField addressField;
	private StringDialogField byteField;

	private static IDialogSettings section;
	private static final String ADDRESS ="Address"; //profile key
	private static final String PROJECT ="Project";
	private static final String BYTES ="Bytes";

	private static final String PAGE_NAME= "WatchBPWizard.page1";

	/**
	 * Constructor for WatchBPWizardPage
	 */
	protected WatchBPWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setDescription(PICLUtils.getResourceString(PAGE_NAME+".description"));
	}

	/**
	 * Constructor for WatchBPWizardPage when editing
	 */
	protected WatchBPWizardPage(String pageName, String title, ImageDescriptor titleImage, IMarker breakpoint) {
		super(pageName, title, titleImage, breakpoint);
		setDescription(PICLUtils.getResourceString(PAGE_NAME+".description"));
	}


	protected void createRequiredFields()
	{
		projectField = new StringButtonDialogField(this);
		projectField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".projectLabel"));
		projectField.setDialogFieldListener(this);
		projectField.setButtonLabel(PICLUtils.getResourceString(PAGE_NAME+".browseLabel"));

		addressField = new StringDialogField();
		addressField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".addressLabel"));
		addressField.setDialogFieldListener(this);

		byteField = new StringDialogField();
		byteField.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".byteLabel"));
		byteField.setDialogFieldListener(this);
	}


	/**
	 * @see WizardPage#createControl
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);

		int nColumns= 3;

		projectField.doFillIntoGrid(composite, nColumns);
		addressField.doFillIntoGrid(composite, nColumns);
		byteField.doFillIntoGrid(composite, nColumns);

		String pageHelpID = PICLUtils.getHelpResourceString("WatchBPWizardPage");
		//sets the help for any helpless widget on the page
		WorkbenchHelp.setHelp(getShell(), new DialogPageContextComputer(this, pageHelpID));
		//set widget specific help, with page help as backup
		WorkbenchHelp.setHelp(projectField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("WatchBPWizardPage.projectField") , pageHelpID });
		WorkbenchHelp.setHelp(projectField.getChangeControl(composite), new Object[] {PICLUtils.getHelpResourceString("WatchBPWizardPage.projectBrowse") , pageHelpID });
		WorkbenchHelp.setHelp(addressField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("WatchBPWizardPage.addressField") , pageHelpID});
		WorkbenchHelp.setHelp(byteField.getTextControl(composite), new Object[] {PICLUtils.getHelpResourceString("WatchBPWizardPage.numBytesField") , pageHelpID});


		restoreSettings();
	}

	public void dialogFieldChanged(DialogField field)
	{
		if(addressField.getText().equals("")  || projectField.getText().equals("") || byteField.getText().equals(""))
			setPageComplete(false);
		else setPageComplete(true);
	}

	/**
	 * Returns the Address or expression specified in the text field.
	 * For use by Wizard to set marker attributes.
	 */
	public String getStartAddress()
	{
		return addressField.getText();
	}

	/** Returns the value currently in the every field.
	 * For use by Wizard to set marker attributes.
	 */
	public Integer getNumBytes()
	{
		try{
			return Integer.valueOf(byteField.getText());
		}
		catch(NumberFormatException e){
			// todo: display error
			return new Integer(1);
		}
	}

	/**
	 * This method initializes the dialog fields with the values of the existing
	 * breakpoint that the user is editing.
	 */
	private void initUsingOldBreakpoint()
	{

		try{		//todo: handle null responses
			addressField.setText((String)existingBP.getAttribute(IPICLDebugConstants.ADDRESS_EXPRESSION) );
			projectField.setText(existingBP.getResource().getProject().getName()); //TODO
		 	projectField.setEnabled(false);
		 	byteField.setText( ((Integer)existingBP.getAttribute(IPICLDebugConstants.NUM_BYTES_MONITORED)).toString() );
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

	  	text = section.get(BYTES);
	  	if( text != null)
	  		byteField.setText(text);
	  	else
	  		byteField.setText("1"); //default

	}

  	/**
	 * @see ISettingsWriter#writeSettings
	 */
	public void writeSettings()
	{
		section.put(ADDRESS, addressField.getText());
		section.put(PROJECT, projectField.getText());
		section.put(BYTES, byteField.getText());
	}


}

