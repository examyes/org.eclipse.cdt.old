package org.eclipse.cdt.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.cpp.ui.internal.builder.ParsePathControl;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.wizards.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;
import org.eclipse.core.resources.*;

import org.eclipse.swt.layout.*;
import java.io.File;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;

import java.util.*;

public class ParsePathWorkbookPage 
{
    public ParsePathControl _pathControl;
    
    public ParsePathWorkbookPage(Composite parent) 
    {
	_pathControl = new ParsePathControl(parent, SWT.NONE);	

	ArrayList paths = CppPlugin.readProperty("DefaultParseIncludePath");
	_pathControl.setPaths(paths);

       	GridLayout layout = new GridLayout();	
	GridData dData = new GridData(GridData.GRAB_HORIZONTAL
				      |GridData.FILL_BOTH);
	dData.heightHint = 260;
	dData.widthHint  = 160;
	_pathControl.setLayout(layout);
	_pathControl.setLayoutData(dData);

    }

    public void setRemote(boolean isRemote)
    {
	_pathControl.setRemote(isRemote);	
    }


    protected Control getControl() 
    {
	return _pathControl;
    }
}
