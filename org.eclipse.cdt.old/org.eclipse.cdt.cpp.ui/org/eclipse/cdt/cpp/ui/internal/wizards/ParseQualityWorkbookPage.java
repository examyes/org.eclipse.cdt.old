package org.eclipse.cdt.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.cpp.ui.internal.preferences.ParseQualityControl;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.wizards.*;

import org.eclipse.swt.layout.*;
import java.io.File;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;

import java.util.*;

public class ParseQualityWorkbookPage 
{
    ParseQualityControl _qualityControl;
    
    public ParseQualityWorkbookPage(Composite parent) 
    {
	_qualityControl = new ParseQualityControl(parent, SWT.NONE);	

       	GridLayout layout = new GridLayout();	
	GridData dData = new GridData(GridData.GRAB_HORIZONTAL | 
				      GridData.FILL_BOTH);
	dData.heightHint = 60;
	dData.widthHint  = 160;
	_qualityControl.setLayout(layout);	
	_qualityControl.setLayoutData(dData);

	setDefaults();
    }


    public void setDefaults()
    {
	CppPlugin plugin      = CppPlugin.getDefault();
	ArrayList preferences = plugin.readProperty("ParseQuality");
	if (preferences.isEmpty())
	    {
		_qualityControl.setSelection(3);
	    }
	else
	    {
		for (int i = 0; i < preferences.size(); i++)
		    {
			String preference = (String)preferences.get(i);
			int value = Integer.parseInt(preference);
			_qualityControl.setSelection(value);
		    }
	    }
    }
    
    public ArrayList getQuality()
    {
	ArrayList preferences = new ArrayList();
	preferences.add("" + _qualityControl.getSelection());
	return preferences;
    }

    protected Control getControl() 
    {
	return _qualityControl;
    }
}
