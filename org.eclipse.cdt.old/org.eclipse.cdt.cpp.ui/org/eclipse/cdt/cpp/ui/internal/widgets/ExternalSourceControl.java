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

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;

import org.eclipse.core.resources.*;
import java.io.*;
import java.util.ArrayList;

public class ExternalSourceControl extends PathControl
{
    public ExternalSourceControl(Composite cnr, int style)
    {
	super(cnr, style);
    }
	
    public void handleEvent(Event e)
    {
	Widget source = e.widget;
	
	if (source == _browseButton)
	    {
		String selectedDirectory = null;
		if (_project != null)
		    {
			if (_project instanceof Repository)
			    {
				DataElement currentDir = ((Repository)_project).getRemoteElement();
				DataElement input = currentDir;
				if (currentDir.getParent() != null)
				    {
					input = currentDir.getParent();	
				    }
				
				DataElementFileDialog dialog = new DataElementFileDialog("Select External Source Path", input);
				dialog.open();
				if (dialog.getReturnCode() == dialog.OK)
				    {
					DataElement selected = dialog.getSelected();
					if (selected != null)
					    {
						selectedDirectory = selected.getSource();
					    }
				    }
			    }		
		    }
		else
		    {
			DirectoryDialog dialog = new DirectoryDialog(this.getShell(), SWT.SAVE);
			dialog.setMessage("Select External Source Path.");
			dialog.setFilterPath("*.*");
			
			selectedDirectory = dialog.open();
		    }
		
		if (selectedDirectory != null)
		    {
			_pathEntry.setText(selectedDirectory);	
			_addButton.setEnabled(true);
		    }
	    }
	else
	    {
		super.handleEvent(e);
	    }
	
    }
    
}
