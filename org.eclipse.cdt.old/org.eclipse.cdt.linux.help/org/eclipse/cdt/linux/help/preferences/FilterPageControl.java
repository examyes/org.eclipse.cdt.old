package org.eclipse.cdt.linux.help.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.cdt.linux.help.*;
import org.eclipse.cdt.linux.help.views.*;

import java.util.*;

public class FilterPageControl
{
    private HelpPlugin _plugin;
    public FilterControl _filterControl;
    
    public FilterPageControl(Composite parent)
    {
	_plugin = HelpPlugin.getDefault();

	_filterControl = new FilterControl(parent,SWT.NONE);
	GridLayout dlayout = new GridLayout();	
	GridData dData = new GridData(GridData.GRAB_HORIZONTAL
				      |GridData.FILL_BOTH);	
	_filterControl.setLayout(dlayout);
	_filterControl.setLayoutData(dData);
    }

    public void performOk()
    {		
	//store all settings
	_filterControl.storeSettings();
	
	ArrayList unfilteredList = _plugin.getList(); // Get unfiltered list
	if (unfilteredList != null)
	    {
		_plugin.getFilter().updateIndexList(unfilteredList); //Filter the indexes
		
		//map filtered indexes to ItemElements
		ArrayList filteredList = _plugin.getFilter().getFilteredResults();
		ResultsViewPart view =_plugin.getView();
		if(view!=null)
		    view.populate(filteredList);
	    }	
    }

    public void performDefaults()
    {
    }

    public Control getControl()
    {
	return _filterControl;
    }
}
