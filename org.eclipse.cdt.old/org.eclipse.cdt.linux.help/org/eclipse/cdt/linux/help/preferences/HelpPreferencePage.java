package org.eclipse.cdt.linux.help.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.ui.*;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class HelpPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private HelpWorkbook _control;
    public HelpPreferencePage() 
    {
    }

    public void init(IWorkbench workbench) 
    {	
    }

    public Control createContents(Composite parent) 
    {	
	_control = new HelpWorkbook(parent, SWT.NONE);
	_control.setLayout(new FillLayout());
	
	//performDefaults();
	return _control;
    }	
    
    public void performDefaults() 
    {
	_control.performDefaults();
    }

    public boolean performOk()
    {
	_control.performOk();

	return true;
    }
}
