package org.eclipse.cdt.cpp.ui.internal.widgets;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.hosts.dialogs.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;

import org.eclipse.core.resources.*;
import java.io.*;
import java.util.ArrayList;

public class LibraryPathControl extends PathControl
{
    public LibraryPathControl(Composite cnr, int style)
    {
	super(cnr, style, "library");
    }
	
    public void handleEvent(Event e)
    {
	Widget source = e.widget;
	
	if (source == _browseButton)
	    {
		String selectedDirectory = null;
		DataStore dataStore = _plugin.getCurrentDataStore();
		if (_project instanceof Repository)
		    {
			if (_project.isOpen())
			    {
				dataStore = ((Repository)_project).getDataStore();
			    }
			else
			    {
				dataStore = null;
			    }
		    }
		if (dataStore != null)
		    {
			DataElement rootDir = dataStore.getHostRoot().get(0).dereference();
			
			DataElementFileDialog dialog = new DataElementFileDialog("Select Library", rootDir);
			dialog.setActionLoader(CppActionLoader.getInstance());
			dialog.open();
			if (dialog.getReturnCode() == dialog.OK)
			    {
				DataElement selected = dialog.getSelected();
				if (selected != null)
				    {
					selectedDirectory = selected.getSource();
				    }
			    }
			if (selectedDirectory != null)
			    {
				_pathEntry.setText(selectedDirectory);	
				_addButton.setEnabled(true);
			    }
		    }
	    }
	else
	    {
		super.handleEvent(e);
	    }
	
    }
    
}
