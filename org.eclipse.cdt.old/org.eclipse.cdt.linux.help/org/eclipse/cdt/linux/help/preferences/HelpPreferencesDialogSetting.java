package org.eclipse.cdt.linux.help.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.linux.help.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

import java.util.*;

public class HelpPreferencesDialogSetting extends Dialog 
{
    private HelpPlugin _plugin;

    public HelpWorkbook _workbookControl;
    //private Button _performDefaultsButton;
    

    public HelpPreferencesDialogSetting(Shell parentShell)
    {
	super(parentShell);
	_plugin=HelpPlugin.getDefault();
    }

    protected void configureShell(Shell newShell)
    {
	super.configureShell(newShell);	
	newShell.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_TITLE));

	//FIXME:configure help here
	// Workbench.setHelp(newShell,new String[] {"stuff"});
    }

    protected Control createDialogArea(Composite parent)
    {
	Composite composite=(Composite)super.createDialogArea(parent);

	GridLayout compositeLayout = new GridLayout();	
	composite.setLayout(compositeLayout);		
		
	_workbookControl = new HelpWorkbook(composite,SWT.NONE);
	GridLayout dlayout = new GridLayout();	
	GridData dData = new GridData(GridData.GRAB_HORIZONTAL
				      |GridData.FILL_BOTH);	
	_workbookControl.setLayout(dlayout);
	_workbookControl.setLayoutData(dData);		

	return composite;
    }    
    /*
    protected void createButtonsForButtonBar(Composite parent)
    {
	super.createButtonsForButtonBar(parent);
    }
    */
     
    public void okPressed()
    {		
	_workbookControl.performOk();
	super.okPressed();
    }   

    protected void cancelPressed()
    {
	//FIXME: ADD stuff here
	super.cancelPressed();
    }

}
