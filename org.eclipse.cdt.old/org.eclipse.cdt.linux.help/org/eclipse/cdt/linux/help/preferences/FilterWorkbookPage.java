package org.eclipse.cdt.linux.help.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class FilterWorkbookPage
{
    FilterPageControl _filterPageControl;

    public FilterWorkbookPage(Composite parent)
    {
	_filterPageControl = new FilterPageControl(parent);	
	/*
	GridLayout layout = new GridLayout();	
	GridData dData = new GridData(GridData.GRAB_HORIZONTAL
				      |GridData.FILL_BOTH);
	dData.heightHint = 260;
	dData.widthHint  = 160;
	_filterPageControl.setLayout(layout);
	_filterPageControl.setLayoutData(dData);
	*/
	}

    protected Control getControl() 
    {
	return _filterPageControl.getControl();
    }

    public void performOk()
    {
	_filterPageControl.performOk();
    }
    public void performDefaults()
    {
	_filterPageControl.performDefaults();
    }

}
