package org.eclipse.cdt.linux.help.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.linux.help.*;

import org.eclipse.cdt.linux.help.display.HelpBrowserUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

import java.util.*;

public class FilterDialogSetting extends Dialog 
{
    private HelpPlugin _plugin;

    public FilterControl _filterControl;
   
    public FilterDialogSetting(Shell parentShell)
    {
	super(parentShell);
	_plugin=HelpPlugin.getDefault();
    }

    protected void configureShell(Shell newShell)
    {
	super.configureShell(newShell);	
	newShell.setText(_plugin.getLocalizedString(IHelpNLConstants.FILTER_TITLE));

	//FIXME:configure help here
	// Workbench.setHelp(newShell,new String[] {"stuff"});
    }

    protected Control createDialogArea(Composite parent)
    {
	Composite composite=(Composite)super.createDialogArea(parent);

	GridLayout compositeLayout = new GridLayout();	
	composite.setLayout(compositeLayout);		
		
	_filterControl = new FilterControl(composite,SWT.NONE);
	GridLayout dlayout = new GridLayout();	
	GridData dData = new GridData(GridData.GRAB_HORIZONTAL
				      |GridData.FILL_BOTH);	
	_filterControl.setLayout(dlayout);
	_filterControl.setLayoutData(dData);		

	return composite;
    }    
     
    public void okPressed()
    {		
	//store all settings
	_filterControl.storeSettings();

	ArrayList unfilteredList= HelpSearch.getList(); // Get unfiltered list
	if (unfilteredList != null)
	    {
		_plugin.getFilter().updateIndexList(unfilteredList); //Filter the indexes
		
		//map filtered indexes to ItemElements
		ArrayList filteredList = _plugin.getFilter().getFilteredResults();
		_plugin.getView().populate(filteredList);
	    }	

	super.okPressed();
    }   

    protected void cancelPressed()
    {
	//FIXME: ADD stuff here

	super.cancelPressed();
    }

}
