package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.ModelInterface;
import org.eclipse.cdt.dstore.core.model.DE;
import org.eclipse.cdt.dstore.core.model.DataElement;
import org.eclipse.cdt.dstore.core.model.DataStore;
import org.eclipse.cdt.dstore.ui.actions.CustomAction;
import org.eclipse.cdt.dstore.ui.resource.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;

import com.ibm.debug.pdt.breakpoints.PICLLineBreakpoint;

public class AddStatementBreakpoint extends CustomAction
{ 
    private IWorkspace _workspace = null;
    private DataElement _statement;
    
    public AddStatementBreakpoint(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
	_workspace = CppPlugin.getPluginWorkspace();
      } 

    public void createBreakpointMarker(DataElement statement) 
    {
	_statement = statement;
	String fileName   = (String)(statement.getElementProperty(DE.P_SOURCE_NAME));
	Integer lineLocation = (Integer)(statement.getElementProperty(DE.P_SOURCE_LOCATION));
	int line = lineLocation.intValue();	
		    
	ModelInterface api = ModelInterface.getInstance();
	IResource file =  api.findFile(fileName);
	if (file != null)
	{
	    if (file instanceof FileResourceElement)
		{
		    IResource root = _workspace.getRoot();
		    new PICLLineBreakpoint(root, fileName, line);
		}
	    else
		{
		    new PICLLineBreakpoint(file,line);
		}
	}	
    }
    
    public void run()
    {
	createBreakpointMarker(_subject);
    }
    
}


