package org.eclipse.cdt.linux.help.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;


public class SearchWorkbookPage 
{
    SearchPageControl _searchPageControl;

    public SearchWorkbookPage(Composite parent)
    {
	_searchPageControl = new SearchPageControl(parent, SWT.NONE);	
	
       	GridLayout layout = new GridLayout();	
	GridData dData = new GridData(GridData.GRAB_HORIZONTAL
				      |GridData.FILL_BOTH);
	dData.heightHint = 260;
	dData.widthHint  = 160;
	_searchPageControl.setLayout(layout);
	_searchPageControl.setLayoutData(dData);
	
    }

    protected Control getControl() 
    {
	return _searchPageControl;
    }

    public void performOk()
    {
	_searchPageControl.performOk();
    }
    public void performDefaults()
    {
	_searchPageControl.performDefaults();
    }
}
