package org.eclipse.cdt.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.cpp.ui.internal.preferences.NameValueTableControl;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;

import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.core.resources.*;
import org.eclipse.swt.layout.*;
import java.io.File;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;

import java.util.*;

public class EnvironmentWorkbookPage 
{
    private Composite             _environmentCanvas;
    private NameValueTableControl _variableControl;
    private NameValueTableControl _systemVariableControl;


    public EnvironmentWorkbookPage(Composite parent) 
    {
	_environmentCanvas = new Composite(parent, SWT.NONE);

	GridLayout glayout = new GridLayout();
	glayout.numColumns = 1;
	_environmentCanvas.setLayout(glayout);
	GridData dp2 = new GridData(GridData.GRAB_HORIZONTAL |GridData.FILL_BOTH);
	dp2.heightHint = 80;
	dp2.widthHint  = 160;
	_environmentCanvas.setLayoutData(dp2);

	_variableControl = new NameValueTableControl(_environmentCanvas, SWT.NONE, "Project Environment");	
       	FillLayout layout = new FillLayout();

	_systemVariableControl = new NameValueTableControl(_environmentCanvas, SWT.NONE, "System Environment", false);	
	FillLayout layout2 = new FillLayout();	
	_systemVariableControl.setLayout(layout2);	
	
    }

    public void setRemote(boolean isRemote)
    {
	setDefaults(!isRemote);
	_systemVariableControl.setVisible(!isRemote);
    }

    public void setDefaults(boolean showSystemEnvironment)
    {
	CppPlugin plugin = CppPlugin.getDefault();
	ArrayList variables = plugin.readProperty("DefaultEnvironment");
	_variableControl.setVariables(variables);

	    {
		DataStore dataStore = plugin.getDataStore();
		DataElement systemInfo = dataStore.findMinerInformation("org.eclipse.cdt.dstore.miners.environment.EnvironmentMiner");
		if (systemInfo != null)
		    {
			DataElement systemEnvironment = systemInfo.get(0);	
			
			ArrayList svariables = new ArrayList();
			for (int i = 0; i < systemEnvironment.getNestedSize(); i++)
			    {
				DataElement var = (DataElement)systemEnvironment.get(i);
				svariables.add(var.getName());
			    }
			
			_systemVariableControl.setVariables(svariables);
		    }
	    }
    }

    
    
    public ArrayList getVariables()
    {
	return _variableControl.getVariables();
    }

    protected Control getControl() 
    {
	return _environmentCanvas;
    }
}
